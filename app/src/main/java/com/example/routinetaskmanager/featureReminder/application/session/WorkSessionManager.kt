package com.example.routinetaskmanager.featureReminder.application.session

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
import androidx.core.content.edit
import com.example.routinetaskmanager.featureReminder.application.notifications.ReminderSessionNotificationUseCase
import java.time.Duration

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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeReminderInSessionById(reminderId: Long): Flow<List<ReminderOccurrence>?> {
        return state.flatMapLatest { sessionState ->
            val startedAtMillis = sessionState.startedAtMillis

            if (!sessionState.isActive || startedAtMillis == null) {
                flowOf(emptyList())
            } else {
                reminderSessionNotificationUseCase.observeSessionOccurrenceOfReminderById(
                    reminderId = reminderId,
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
        val expiresAtMillis = startedAtMillis + MAX_SESSION_DURATION_MILLIS

        val result = reminderSessionNotificationUseCase.startSession(
            startedAt = startedAtMillis.toLocalDateTime()
        )

        val newState = WorkSessionState(
            isActive = true,
            startedAtMillis = startedAtMillis,
            expiresAtMillis = expiresAtMillis,
            sessionReminderCount = result.sessionReminderCount,
            scheduledNotificationCount = result.scheduledNotificationCount
        )

        _state.value = newState
        persistState(newState)

        return newState
    }

    suspend fun rescheduleActiveSessionIfNeeded(): Boolean {
        val startedAtMillis = _state.value.startedAtMillis ?: return false
        if (!_state.value.isActive) {
            return false
        }

        reminderSessionNotificationUseCase.startSession(
            startedAt = startedAtMillis.toLocalDateTime(),
            from = LocalDateTime.now()
        )

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
            ?: return WorkSessionState()

        val savedExpiresAtMillis = prefs.getLong(KEY_EXPIRES_AT_MILLIS, 0L)
            .takeIf { it > 0L }

        val expiresAtMillis = savedExpiresAtMillis
            ?: (startedAtMillis + MAX_SESSION_DURATION_MILLIS)

        val now = System.currentTimeMillis()

        if (now >= expiresAtMillis) {
            clearPersistedState()
            return WorkSessionState()
        }

        return WorkSessionState(
            isActive = true,
            startedAtMillis = startedAtMillis,
            expiresAtMillis = expiresAtMillis
        )
    }


    private fun persistState(state: WorkSessionState) {
        prefs.edit {
            if (state.isActive && state.startedAtMillis != null && state.expiresAtMillis != null) {
                putLong(KEY_STARTED_AT_MILLIS, state.startedAtMillis)
                putLong(KEY_EXPIRES_AT_MILLIS, state.expiresAtMillis)
            } else {
                remove(KEY_STARTED_AT_MILLIS)
                remove(KEY_EXPIRES_AT_MILLIS)
            }
        }
    }

    private fun clearPersistedState() {
        prefs.edit {
            remove(KEY_STARTED_AT_MILLIS)
                .remove(KEY_EXPIRES_AT_MILLIS)
        }
    }

    private fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private companion object {
        const val PREFS_NAME = "work_session"
        const val KEY_STARTED_AT_MILLIS = "started_at_millis"
        const val KEY_EXPIRES_AT_MILLIS = "expires_at_millis"

        val MAX_SESSION_DURATION_MILLIS: Long = Duration.ofHours(24).toMillis()
    }
}

data class WorkSessionState(
    val isActive: Boolean = false,
    val startedAtMillis: Long? = null,
    val expiresAtMillis: Long? = null,
    val sessionReminderCount: Int = 0,
    val scheduledNotificationCount: Int = 0
)
