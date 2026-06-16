package com.example.routinetaskmanager.featureReminder.domain.useCase

import android.content.Context
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WorkSessionManager(
    context: Context,
    private val reminderSessionNotificationUseCase: ReminderSessionNotificationUseCase
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadPersistedState())
    val state: StateFlow<WorkSessionState> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeActiveSessionOccurrences(): Flow<List<ReminderOccurrence>> {
        return state.flatMapLatest { sessionState ->
            val startedAtMillis = sessionState.startedAtMillis

            if (!sessionState.isActive || startedAtMillis == null) {
                flowOf(emptyList())
            } else {
                reminderSessionNotificationUseCase.observeSessionOccurrences(
                    startedAt = startedAtMillis.toLocalDateTime()
                )
            }
        }
    }

    suspend fun refreshSessionReminderCount() {
        val count = reminderSessionNotificationUseCase.countSessionReminders(
            startedAt = _state.value.startedAtMillis?.toLocalDateTime() ?: LocalDateTime.now()
        )

        _state.update { current ->
            current.copy(sessionReminderCount = count)
        }
    }

    suspend fun startOrRestartSession(): WorkSessionState {
        val startedAtMillis = _state.value.startedAtMillis ?: System.currentTimeMillis()
        val result = reminderSessionNotificationUseCase.startSession(
            startedAt = startedAtMillis.toLocalDateTime()
        )

        val newState = WorkSessionState(
            isActive = true,
            startedAtMillis = startedAtMillis,
            sessionReminderCount = result.sessionReminderCount,
            scheduledNotificationCount = result.scheduledNotificationCount
        )

        _state.value = newState
        persistState(newState)

        return newState
    }

    suspend fun rescheduleActiveSessionIfNeeded(): Boolean {
        if (!_state.value.isActive) {
            return false
        }

        startOrRestartSession()

        return true
    }

    suspend fun endSession(): WorkSessionState {
        reminderSessionNotificationUseCase.endSession()

        val count = reminderSessionNotificationUseCase.countSessionReminders()
        val newState = WorkSessionState(
            isActive = false,
            startedAtMillis = null,
            sessionReminderCount = count,
            scheduledNotificationCount = 0
        )

        _state.value = newState
        persistState(newState)

        return newState
    }

    private fun loadPersistedState(): WorkSessionState {
        val startedAtMillis = prefs.getLong(KEY_STARTED_AT_MILLIS, 0L)
            .takeIf { it > 0L }

        return if (startedAtMillis == null) {
            WorkSessionState()
        } else {
            WorkSessionState(
                isActive = true,
                startedAtMillis = startedAtMillis
            )
        }
    }

    private fun persistState(state: WorkSessionState) {
        prefs.edit().apply {
            if (state.isActive && state.startedAtMillis != null) {
                putLong(KEY_STARTED_AT_MILLIS, state.startedAtMillis)
            } else {
                remove(KEY_STARTED_AT_MILLIS)
            }
        }.apply()
    }

    private fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private companion object {
        const val PREFS_NAME = "work_session"
        const val KEY_STARTED_AT_MILLIS = "started_at_millis"
    }
}

data class WorkSessionState(
    val isActive: Boolean = false,
    val startedAtMillis: Long? = null,
    val sessionReminderCount: Int = 0,
    val scheduledNotificationCount: Int = 0
)
