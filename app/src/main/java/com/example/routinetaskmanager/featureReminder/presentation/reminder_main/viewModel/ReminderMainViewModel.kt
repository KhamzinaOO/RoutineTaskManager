package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.example.routinetaskmanager.featureReminder.application.session.RestoreWorkSessionRuntimeResult
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionResult
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveDayReminderOccurrencesUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveWorkSessionStateUseCase
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainIntent
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderMainViewModel(
    private val observeDayReminderOccurrencesUseCase: ObserveDayReminderOccurrencesUseCase,
    private val observeWorkSessionStateUseCase: ObserveWorkSessionStateUseCase,
    private val restoreActiveWorkSessionRuntimeUseCase: RestoreActiveWorkSessionRuntimeUseCase,
    private val toggleWorkSessionUseCase: ToggleWorkSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderMainUiState())
    val uiState: StateFlow<ReminderMainUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ReminderMainEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var remindersJob: Job? = null

    init {
        observeWorkSession()
        observeRemindersForDate(_uiState.value.selectedDate)
        restoreActiveWorkSessionRuntime()
    }

    fun onIntent(intent: ReminderMainIntent) {
        when (intent) {
            ReminderMainIntent.AddFABClick -> {
                sendEffect(ReminderMainEffect.FABClicked)
            }

            ReminderMainIntent.MenuButtonClick -> {
                sendEffect(ReminderMainEffect.OpenDrawer)
            }

            ReminderMainIntent.SearchButtonClick,
            ReminderMainIntent.CalendarButtonClick,
            ReminderMainIntent.CalendarSwipe -> Unit

            is ReminderMainIntent.DateClick -> {
                selectDate(intent.date)
            }

            ReminderMainIntent.SessionButtonClick,
            ReminderMainIntent.EndSessionButtonClick -> {
                toggleWorkSession()
            }

            ReminderMainIntent.NotificationPermissionDenied -> {
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        UiText.StringResource(R.string.notifications_disabled_enable_settings)
                    )
                )
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (_uiState.value.selectedDate == date) return

        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                reminders = emptyList()
            )
        }

        observeRemindersForDate(date)
    }

    private fun observeRemindersForDate(date: LocalDate) {
        remindersJob?.cancel()

        remindersJob = viewModelScope.launch {
            runCatching {
                observeDayReminderOccurrencesUseCase(date)
            }.onSuccess { remindersFlow ->
                remindersFlow
                    .onEach { reminders ->
                        _uiState.update { state ->
                            state.copy(reminders = reminders)
                        }
                    }
                    .catch { throwable ->
                        showLoadRemindersError(throwable)
                    }
                    .collect {}
            }.onFailure { throwable ->
                showLoadRemindersError(throwable)
            }
        }
    }

    private fun observeWorkSession() {
        observeWorkSessionStateUseCase()
            .onEach { sessionState ->
                _uiState.update { state ->
                    state.copy(
                        isSessionActive = sessionState.isActive,
                        sessionStartedAtMillis = sessionState.startedAtMillis,
                        sessionReminderCount = sessionState.sessionReminderCount
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun restoreActiveWorkSessionRuntime() {
        viewModelScope.launch {
            when (restoreActiveWorkSessionRuntimeUseCase()) {
                RestoreWorkSessionRuntimeResult.NotActive,
                RestoreWorkSessionRuntimeResult.Restored -> Unit

                is RestoreWorkSessionRuntimeResult.Failed -> {
                    sendEffect(
                        ReminderMainEffect.ShowMessage(
                            UiText.StringResource(R.string.error_failed_restore_work_session_service)
                        )
                    )
                }
            }
        }
    }

    private fun toggleWorkSession() {
        viewModelScope.launch {
            if (_uiState.value.isSessionActionInProgress) return@launch

            _uiState.update { state ->
                state.copy(isSessionActionInProgress = true)
            }

            val result = runCatching {
                toggleWorkSessionUseCase()
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.Failed(throwable)
            }

            handleToggleWorkSessionResult(result)

            _uiState.update { state ->
                state.copy(isSessionActionInProgress = false)
            }
        }
    }

    private fun handleToggleWorkSessionResult(
        result: ToggleWorkSessionResult
    ) {
        when (result) {
            is ToggleWorkSessionResult.Started -> {
                val message = if (result.wasRestart) {
                    UiText.PluralResource(
                        R.plurals.work_session_restarted_count,
                        result.scheduledNotificationCount
                    )
                } else {
                    UiText.PluralResource(
                        R.plurals.work_session_started_scheduled_count,
                        result.scheduledNotificationCount
                    )
                }

                sendEffect(ReminderMainEffect.ShowMessage(message))
            }

            ToggleWorkSessionResult.StartedWithoutReminders -> {
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        UiText.StringResource(R.string.work_session_started_no_session_reminders)
                    )
                )
            }

            ToggleWorkSessionResult.Ended -> {
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        UiText.StringResource(R.string.work_session_ended)
                    )
                )
            }

            ToggleWorkSessionResult.ForegroundStartBlocked -> {
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        UiText.StringResource(R.string.error_failed_start_work_session_service)
                    )
                )
            }

            is ToggleWorkSessionResult.Failed -> {
                sendEffect(
                    ReminderMainEffect.ShowMessage(
                        result.throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.error_failed_start_work_session)
                    )
                )
            }
        }
    }

    private fun showLoadRemindersError(
        throwable: Throwable
    ) {
        sendEffect(
            ReminderMainEffect.ShowMessage(
                throwable.message?.let(UiText::DynamicString)
                    ?: UiText.StringResource(R.string.error_failed_load_reminders)
            )
        )
    }

    private fun sendEffect(
        effect: ReminderMainEffect
    ) {
        _effect.trySend(effect)
    }
}