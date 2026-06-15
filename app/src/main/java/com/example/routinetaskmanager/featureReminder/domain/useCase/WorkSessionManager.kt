package com.example.routinetaskmanager.featureReminder.domain.useCase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WorkSessionManager(
    private val reminderSessionNotificationUseCase: ReminderSessionNotificationUseCase
) {

    private val _state = MutableStateFlow(WorkSessionState())
    val state: StateFlow<WorkSessionState> = _state.asStateFlow()

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

        return newState
    }

    private fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}

data class WorkSessionState(
    val isActive: Boolean = false,
    val startedAtMillis: Long? = null,
    val sessionReminderCount: Int = 0,
    val scheduledNotificationCount: Int = 0
)
