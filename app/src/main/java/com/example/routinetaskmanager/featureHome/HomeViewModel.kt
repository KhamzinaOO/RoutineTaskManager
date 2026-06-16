package com.example.routinetaskmanager.featureHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.core.notifications.WorkSessionForegroundController
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveReminderScheduleUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.WorkSessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeViewModel(
    private val observeReminderScheduleUseCase: ObserveReminderScheduleUseCase,
    private val reminderCommandUseCase: ReminderCommandUseCase,
    private val workSessionManager: WorkSessionManager,
    private val workSessionForegroundController: WorkSessionForegroundController
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            greetingText = buildGreeting(),
            dateText = buildDateText()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeWorkSession()
        observeTodayReminders()
        refreshSessionReminderCount()
        ensureForegroundServiceForActiveSession()
    }

    fun onSessionButtonClick() {
        viewModelScope.launch {
            if (_uiState.value.isSessionActionInProgress) {
                return@launch
            }

            val wasActive = _uiState.value.isSessionActive
            _uiState.update { it.copy(isSessionActionInProgress = true) }

            runCatching {
                reminderCommandUseCase.startWorkSession()
            }.onSuccess { sessionState ->
                sessionState.startedAtMillis?.let(workSessionForegroundController::start)

                val message = when {
                    sessionState.scheduledNotificationCount == 0 -> {
                        "Work session started. No session reminders scheduled"
                    }

                    wasActive -> {
                        "Work session reminders restarted: ${sessionState.scheduledNotificationCount}"
                    }

                    else -> {
                        "Work session started. Scheduled reminders: ${sessionState.scheduledNotificationCount}"
                    }
                }

                sendEffect(HomeEffect.ShowMessage(message))
            }.onFailure { throwable ->
                sendEffect(
                    HomeEffect.ShowMessage(
                        throwable.message ?: "Failed to start work session"
                    )
                )
            }.also {
                _uiState.update { it.copy(isSessionActionInProgress = false) }
            }
        }
    }

    fun onEndSessionButtonClick() {
        viewModelScope.launch {
            if (_uiState.value.isSessionActionInProgress) {
                return@launch
            }

            _uiState.update { it.copy(isSessionActionInProgress = true) }

            runCatching {
                reminderCommandUseCase.endWorkSession()
            }.onSuccess {
                workSessionForegroundController.stop()
                sendEffect(HomeEffect.ShowMessage("Work session ended"))
            }.onFailure { throwable ->
                sendEffect(
                    HomeEffect.ShowMessage(
                        throwable.message ?: "Failed to end work session"
                    )
                )
            }.also {
                _uiState.update { it.copy(isSessionActionInProgress = false) }
            }
        }
    }

    fun onNotificationPermissionDenied() {
        sendEffect(
            HomeEffect.ShowMessage(
                "Notifications are disabled. Enable them in settings to receive reminders"
            )
        )
    }

    fun onExactAlarmAccessDenied() {
        sendEffect(
            HomeEffect.ShowMessage(
                "Exact alarm access is off. Reminders may arrive a little later"
            )
        )
    }

    private fun observeWorkSession() {
        workSessionManager.state
            .onEach { sessionState ->
                _uiState.update {
                    it.copy(
                        isSessionActive = sessionState.isActive,
                        sessionStartedAtMillis = sessionState.startedAtMillis,
                        sessionReminderCount = sessionState.sessionReminderCount
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun ensureForegroundServiceForActiveSession() {
        workSessionManager.state.value.startedAtMillis?.let { startedAtMillis ->
            if (workSessionManager.state.value.isActive) {
                workSessionForegroundController.start(startedAtMillis)
            }
        }
    }

    private fun observeTodayReminders() {
        viewModelScope.launch {
            runCatching {
                val today = LocalDate.now()

                combine(
                    observeReminderScheduleUseCase(range = dayRange(today)),
                    workSessionManager.observeActiveSessionOccurrences()
                ) { scheduledReminders, sessionReminders ->
                    mergeReminderOccurrences(
                        scheduledReminders = scheduledReminders,
                        sessionReminders = sessionReminders,
                        date = today
                    )
                }
            }.onSuccess { remindersFlow ->
                remindersFlow.distinctUntilChanged().collect { reminders ->
                    _uiState.update {
                        it.copy(reminders = reminders)
                    }
                    refreshSessionReminderCount()
                }
            }.onFailure { throwable ->
                sendEffect(
                    HomeEffect.ShowMessage(
                        throwable.message ?: "Failed to load reminders"
                    )
                )
            }
        }
    }

    private fun mergeReminderOccurrences(
        scheduledReminders: List<ReminderOccurrence>,
        sessionReminders: List<ReminderOccurrence>,
        date: LocalDate
    ): List<ReminderOccurrence> {
        return (scheduledReminders + sessionReminders.filter { occurrence ->
            occurrence.scheduledAt.toLocalDate() == date
        })
            .distinctBy { occurrence ->
                "${occurrence.reminderId}-${occurrence.scheduledAt}-${occurrence.repeatType}"
            }
            .sortedBy { occurrence -> occurrence.scheduledAt }
    }

    private fun refreshSessionReminderCount() {
        viewModelScope.launch {
            runCatching {
                workSessionManager.refreshSessionReminderCount()
            }
        }
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun buildGreeting(): String {
        val hour = LocalTime.now().hour

        return when (hour) {
            in 5..11 -> "Good morning!"
            in 12..16 -> "Good afternoon!"
            in 17..21 -> "Good evening!"
            else -> "Good night!"
        }
    }

    private fun buildDateText(): String {
        val date = LocalDate.now()
        val locale = Locale.getDefault()
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val dateText = date.format(DateTimeFormatter.ofPattern("dd MMMM y", locale))

        return "$dayOfWeek, $dateText"
    }
}

sealed interface HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect
}
