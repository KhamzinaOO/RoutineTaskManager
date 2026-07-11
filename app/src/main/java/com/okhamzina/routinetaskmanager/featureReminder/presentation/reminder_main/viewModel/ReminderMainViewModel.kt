package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.onErrorMessage
import com.okhamzina.routinetaskmanager.core.error.onSuccess
import com.okhamzina.routinetaskmanager.core.error.runAppCatching
import com.okhamzina.routinetaskmanager.core.error.runSuspendCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.core.time.DateTimeTicker
import com.okhamzina.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionResult
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveDayReminderOccurrencesUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ObserveWorkSessionStateUseCase
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.mappers.toUiMessage
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.mappers.toUiMessageOrNull
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainUiState
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
    private val toggleWorkSessionUseCase: ToggleWorkSessionUseCase,
    private val dateTimeTicker: DateTimeTicker,
    private val errorReporter: ErrorReporter
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

            ReminderMainIntent.SessionButtonClick -> {
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
            runAppCatching(errorReporter) {
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
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_load_reminders)
            ) { message ->
                sendEffect(ReminderMainEffect.ShowMessage(message))
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
            restoreActiveWorkSessionRuntimeUseCase().toUiMessageOrNull()?.let { message ->
                sendEffect(ReminderMainEffect.ShowMessage(message))
            }
        }
    }

    private fun toggleWorkSession() {
        viewModelScope.launch {
            if (_uiState.value.isSessionActionInProgress) return@launch

            _uiState.update { state ->
                state.copy(isSessionActionInProgress = true)
            }

            val result = runSuspendCatching(errorReporter) {
                toggleWorkSessionUseCase()
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.StartFailed(throwable.toAppError())
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
        sendEffect(ReminderMainEffect.ShowMessage(result.toUiMessage()))
    }

    private fun showLoadRemindersError(
        throwable: Throwable
    ) {
        sendEffect(
            ReminderMainEffect.ShowMessage(
                throwable.toAppError().toUiText(
                    defaultMessage = UiText.StringResource(R.string.error_failed_load_reminders)
                )
            )
        )
    }

    private fun sendEffect(
        effect: ReminderMainEffect
    ) {
        _effect.trySend(effect)
    }
}
