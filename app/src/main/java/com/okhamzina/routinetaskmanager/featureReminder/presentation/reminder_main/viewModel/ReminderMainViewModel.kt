package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.onErrorMessage
import com.okhamzina.routinetaskmanager.core.error.runSuspendCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.core.presentation.model.MviViewModel
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate

class ReminderMainViewModel(
    private val observeDayReminderOccurrencesUseCase: ObserveDayReminderOccurrencesUseCase,
    private val observeWorkSessionStateUseCase: ObserveWorkSessionStateUseCase,
    private val restoreActiveWorkSessionRuntimeUseCase: RestoreActiveWorkSessionRuntimeUseCase,
    private val toggleWorkSessionUseCase: ToggleWorkSessionUseCase,
    private val dateTimeTicker: DateTimeTicker,
    private val errorReporter: ErrorReporter
) : MviViewModel<ReminderMainUiState, ReminderMainIntent, ReminderMainEffect>(
    ReminderMainUiState()
) {

    init {
        observeWorkSession()
        observeToday()
        observeReminders()
        restoreActiveWorkSessionRuntime()
    }

    override fun onIntent(intent: ReminderMainIntent) {
        when (intent) {
            ReminderMainIntent.AddReminderClicked -> {
                sendEffect(ReminderMainEffect.NavigateToCreateReminder)
            }

            ReminderMainIntent.MenuButtonClicked -> {
                sendEffect(ReminderMainEffect.OpenDrawer)
            }

            ReminderMainIntent.SearchButtonClicked -> {
                sendEffect(ReminderMainEffect.OpenSearch)
            }

            ReminderMainIntent.CalendarButtonClicked -> {
                sendEffect(ReminderMainEffect.OpenCalendar)
            }

            is ReminderMainIntent.DateSelected -> {
                selectDate(intent.date)
            }

            ReminderMainIntent.SessionButtonClicked -> {
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
        if (currentState.selectedDate == date) return

        updateState { state ->
            state.copy(
                selectedDate = date
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeReminders() {
        uiState
            .map { state -> state.selectedDate }
            .distinctUntilChanged()
            .flatMapLatest { date ->
                observeDayReminderOccurrencesUseCase(date)
            }
            .onEach { reminders ->
                updateState { state -> state.copy(reminders = reminders) }
            }
            .catch { throwable ->
                errorReporter.record(throwable)
                showLoadRemindersError(throwable)
            }
            .launchIn(viewModelScope)
    }

    private fun observeToday() {
        dateTimeTicker.todayFlow()
            .onEach { today ->
                val followsToday = currentState.selectedDate == currentState.today
                updateState { state ->
                    state.copy(
                        today = today,
                        selectedDate = if (followsToday) today else state.selectedDate
                    )
                }
            }
            .catch { throwable ->
                errorReporter.record(throwable)
                showLoadRemindersError(throwable)
            }
            .launchIn(viewModelScope)
    }

    private fun observeWorkSession() {
        observeWorkSessionStateUseCase()
            .onEach { sessionState ->
                updateState { state ->
                    state.copy(
                        isSessionActive = sessionState.isActive,
                        sessionStartedAtMillis = sessionState.startedAtMillis,
                        sessionReminderCount = sessionState.sessionReminderCount
                    )
                }
            }
            .catch { throwable ->
                errorReporter.record(throwable)
                showLoadRemindersError(throwable)
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
            if (currentState.isSessionActionInProgress) return@launch

            updateState { state ->
                state.copy(isSessionActionInProgress = true)
            }

            try {
                val result = runSuspendCatching(errorReporter) {
                    toggleWorkSessionUseCase()
                }.getOrElse { throwable ->
                    ToggleWorkSessionResult.StartFailed(throwable.toAppError())
                }

                handleToggleWorkSessionResult(result)
            } finally {
                updateState { state ->
                    state.copy(isSessionActionInProgress = false)
                }
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

}
