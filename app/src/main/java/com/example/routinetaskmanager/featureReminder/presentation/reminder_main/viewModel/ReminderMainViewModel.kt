package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.core.notifications.WorkSessionForegroundController
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveReminderScheduleUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.WorkSessionManager
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainIntent
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainUiState
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderMainViewModel(
    private val reminderCommandUseCase: ReminderCommandUseCase,
    private val workSessionManager: WorkSessionManager,
    private val reminderUseCase : ObserveReminderScheduleUseCase,
    private val workSessionForegroundController: WorkSessionForegroundController
) : ViewModel() {

    private val _effect = Channel<ReminderMainEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(ReminderMainUiState())
    val uiState : StateFlow<ReminderMainUiState> = _uiState.asStateFlow()
    private var remindersJob: Job? = null

    init {
        observeWorkSession()
        loadReminders(_uiState.value.selectedDate)
        refreshSessionReminderCount()
        ensureForegroundServiceForActiveSession()
    }

    private fun loadReminders(date: LocalDate){
        remindersJob?.cancel()
        remindersJob = viewModelScope.launch {
            runCatching {
                combine(
                    reminderUseCase.invoke(range = dayRange(date)),
                    workSessionManager.observeActiveSessionOccurrences()
                ) { scheduledReminders, sessionReminders ->
                    mergeReminderOccurrences(
                        scheduledReminders = scheduledReminders,
                        sessionReminders = sessionReminders,
                        date = date
                    )
                }
            }.onSuccess {
                it.distinctUntilChanged().collect { reminders ->
                    _uiState.update {
                        it.copy(
                            reminders = reminders
                        )
                    }
                    refreshSessionReminderCount()
                }
            }.onFailure { throwable ->
                sendEffect(
                    ReminderMainEffect.ShowMessage(
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

    fun onIntent(intent : ReminderMainIntent) {
        when(intent) {
            is ReminderMainIntent.AddFABClick -> {
                sendEffect(ReminderMainEffect.FABClicked)
            }
            is ReminderMainIntent.CalendarButtonClick -> Unit
            is ReminderMainIntent.CalendarSwipe -> Unit
            is ReminderMainIntent.DateClick -> {
                selectDate(intent.date)
            }
            is ReminderMainIntent.EndSessionButtonClick -> {
                endSession()
            }
            is ReminderMainIntent.ExactAlarmAccessDenied -> {
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        "Exact alarm access is off. Reminders may arrive a little later"
                    )
                )
            }
            is ReminderMainIntent.MenuButtonClick -> {
                sendEffect(ReminderMainEffect.OpenDrawer)
            }
            is ReminderMainIntent.NotificationPermissionDenied -> {
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        "Notifications are disabled. Enable them in settings to receive reminders"
                    )
                )
            }
            is ReminderMainIntent.SearchButtonClick -> Unit
            is ReminderMainIntent.SessionButtonClick -> {
                startOrRestartSession()
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (_uiState.value.selectedDate == date) return

        _uiState.update {
            it.copy(
                selectedDate = date,
                reminders = emptyList()
            )
        }
        loadReminders(date)
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

    private fun refreshSessionReminderCount() {
        viewModelScope.launch {
            runCatching {
                workSessionManager.refreshSessionReminderCount()
            }
        }
    }

    private fun startOrRestartSession() {
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

                sendEffect(ReminderMainEffect.ShowMessage(message))
            }.onFailure { throwable ->
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        throwable.message ?: "Failed to start work session"
                    )
                )
            }.also {
                _uiState.update { it.copy(isSessionActionInProgress = false) }
            }
        }
    }

    private fun endSession() {
        viewModelScope.launch {
            if (_uiState.value.isSessionActionInProgress) {
                return@launch
            }

            _uiState.update { it.copy(isSessionActionInProgress = true) }

            runCatching {
                reminderCommandUseCase.endWorkSession()
            }.onSuccess {
                workSessionForegroundController.stop()
                sendEffect(ReminderMainEffect.ShowMessage("Work session ended"))
            }.onFailure { throwable ->
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        throwable.message ?: "Failed to end work session"
                    )
                )
            }.also {
                _uiState.update { it.copy(isSessionActionInProgress = false) }
            }
        }
    }

    private fun sendEffect(
        effect: ReminderMainEffect
    ) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
