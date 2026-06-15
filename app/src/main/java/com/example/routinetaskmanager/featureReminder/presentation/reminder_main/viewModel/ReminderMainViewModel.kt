package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderMainViewModel(
    private val reminderCommandUseCase: ReminderCommandUseCase,
    private val workSessionManager: WorkSessionManager,
    private val reminderUseCase : ObserveReminderScheduleUseCase
) : ViewModel() {

    private val _effect = Channel<ReminderMainEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(ReminderMainUiState())
    val uiState : StateFlow<ReminderMainUiState> = _uiState.asStateFlow()

    init {
        observeWorkSession()
        loadReminders()
        refreshSessionReminderCount()
    }

    fun loadReminders(){
        viewModelScope.launch {
            runCatching {
                reminderUseCase.invoke(range = dayRange(LocalDate.now()))
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

    fun onIntent(intent : ReminderMainIntent) {
        when(intent) {
            is ReminderMainIntent.AddFABClick -> {
                sendEffect(ReminderMainEffect.FABClicked)
            }
            is ReminderMainIntent.CalendarButtonClick -> Unit
            is ReminderMainIntent.CalendarSwipe -> Unit
            is ReminderMainIntent.DateClick -> Unit
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

    private fun refreshSessionReminderCount() {
        viewModelScope.launch {
            runCatching {
                workSessionManager.refreshSessionReminderCount()
            }
        }
    }

    private fun startOrRestartSession() {
        viewModelScope.launch {
            val wasActive = _uiState.value.isSessionActive

            runCatching {
                reminderCommandUseCase.startWorkSession()
            }.onSuccess { sessionState ->
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
            }
        }
    }

    private fun endSession() {
        viewModelScope.launch {
            runCatching {
                reminderCommandUseCase.endWorkSession()
            }.onSuccess {
                sendEffect(ReminderMainEffect.ShowMessage("Work session ended"))
            }.onFailure { throwable ->
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        throwable.message ?: "Failed to end work session"
                    )
                )
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
