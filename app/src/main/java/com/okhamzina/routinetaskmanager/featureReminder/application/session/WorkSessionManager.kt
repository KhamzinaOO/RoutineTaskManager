package com.okhamzina.routinetaskmanager.featureReminder.application.session

import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.map
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.ReminderSessionNotificationUseCase
import java.time.Duration

class WorkSessionManager(
    private val stateStore: WorkSessionStateStore,
    private val reminderSessionNotificationUseCase: ReminderSessionNotificationUseCase
) {

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

    suspend fun startSession(): AppResult<WorkSessionState, AppError> {
        val startedAtMillis = System.currentTimeMillis()
        return scheduleSessionFrom(
            state = WorkSessionState(
                isActive = true,
                startedAtMillis = startedAtMillis,
                expiresAtMillis = startedAtMillis + MAX_SESSION_DURATION_MILLIS
            ),
            from = startedAtMillis.toLocalDateTime()
        )
    }

    suspend fun rescheduleActiveSessionIfNeeded(): AppResult<Boolean, AppError> {
        val currentState = _state.value
        val startedAtMillis = currentState.startedAtMillis
            ?: return AppResult.Success(false)

        if (!currentState.isActive) {
            return AppResult.Success(false)
        }

        return scheduleSessionFrom(
            state = currentState.copy(
                expiresAtMillis = currentState.expiresAtMillis
                    ?: (startedAtMillis + MAX_SESSION_DURATION_MILLIS)
            ),
            from = LocalDateTime.now()
        ).map { true }
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

    private suspend fun scheduleSessionFrom(
        state: WorkSessionState,
        from: LocalDateTime
    ): AppResult<WorkSessionState, AppError> {
        val startedAtMillis = requireNotNull(state.startedAtMillis) {
            "Active work session must have start time"
        }

        val result = when (
            val scheduleResult = reminderSessionNotificationUseCase.rescheduleSessionNotifications(
                startedAt = startedAtMillis.toLocalDateTime(),
                from = from
            )
        ) {
            is AppResult.Error -> return AppResult.Error(scheduleResult.error)
            is AppResult.Success -> scheduleResult.data
        }

        val newState = state.copy(
            isActive = true,
            startedAtMillis = startedAtMillis,
            expiresAtMillis = state.expiresAtMillis
                ?: (startedAtMillis + MAX_SESSION_DURATION_MILLIS),
            sessionReminderCount = result.sessionReminderCount,
            scheduledNotificationCount = result.scheduledNotificationCount
        )

        _state.value = newState
        persistState(newState)

        return AppResult.Success(newState)
    }

    private fun loadPersistedState(): WorkSessionState {
        val persistedState = stateStore.load()
            ?: return WorkSessionState()

        val startedAtMillis = persistedState.startedAtMillis

        val expiresAtMillis = persistedState.expiresAtMillis
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
        if (state.isActive && state.startedAtMillis != null && state.expiresAtMillis != null) {
            stateStore.save(
                PersistedWorkSessionState(
                    startedAtMillis = state.startedAtMillis,
                    expiresAtMillis = state.expiresAtMillis
                )
            )
        } else {
            stateStore.clear()
        }
    }

    private fun clearPersistedState() {
        stateStore.clear()
    }

    private fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private companion object {
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
