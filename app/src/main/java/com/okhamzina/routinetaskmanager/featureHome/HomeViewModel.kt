package com.okhamzina.routinetaskmanager.featureHome

import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.onErrorMessage
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.error.runSuspendCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.core.presentation.model.MviViewModel
import com.okhamzina.routinetaskmanager.core.time.DateTimeTicker
import com.okhamzina.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveDayReminderOccurrencesUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ObserveWorkSessionStateUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionResult
import com.okhamzina.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.mappers.toUiMessage
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.mappers.toUiMessageOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeViewModel(
    private val observeDayReminderOccurrencesUseCase: ObserveDayReminderOccurrencesUseCase,
    private val observeNextReminderOccurrenceUseCase: ObserveNextReminderOccurrenceUseCase,
    private val reminderCommandUseCase: ReminderCommandUseCase,
    private val observeWorkSessionStateUseCase: ObserveWorkSessionStateUseCase,
    private val restoreActiveWorkSessionRuntimeUseCase: RestoreActiveWorkSessionRuntimeUseCase,
    private val toggleWorkSessionUseCase: ToggleWorkSessionUseCase,
    private val dateTimeTicker: DateTimeTicker,
    private val errorReporter: ErrorReporter
) : MviViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {

    init {
        observeTodayReminders()
        observeNextReminder()
        observeWorkSession()
        restoreActiveWorkSessionRuntime()
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.AddScheduleItemClicked -> {
                sendEffect(
                    when (currentState.selectedScheduleSection) {
                        HomeScheduleSection.REMINDERS -> HomeEffect.NavigateCreateReminder
                        HomeScheduleSection.TASKS -> HomeEffect.NavigateTasks
                    }
                )
            }

            is HomeIntent.ScheduleSectionSelected -> {
                updateState { state ->
                    state.copy(selectedScheduleSection = intent.section)
                }
            }

            HomeIntent.NotificationPermissionDenied -> {
                sendEffect(
                    HomeEffect.ShowMessage(
                        UiText.StringResource(R.string.error_notification_permission_denied)
                    )
                )
            }

            HomeIntent.SettingsClicked -> {
                sendEffect(HomeEffect.NavigateToSettings)
            }

            is HomeIntent.NextReminderDoneClicked -> {
                completeOccurrence(intent.occurrence)
            }

            is HomeIntent.NextReminderSkipClicked -> {
                skipOccurrence(intent.occurrence)
            }

            HomeIntent.SessionButtonClicked -> {
                toggleWorkSession()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodayReminders() {
        dateTimeTicker.nowMinuteFlow()
            .onEach { now ->
                updateState {
                    it.copy(
                        currentDateTime = now,
                        greetingText = buildGreeting(now.toLocalTime()),
                        dateText = buildDateText(now.toLocalDate())
                    )
                }
            }
            .map { now -> now.toLocalDate() }
            .distinctUntilChanged()
            .flatMapLatest { today ->
                observeDayReminderOccurrencesUseCase(date = today)
            }
            .onEach { reminders ->
                updateState {
                    it.copy(reminders = reminders)
                }
            }
            .catch { throwable ->
                errorReporter.record(throwable)
                sendEffect(
                    HomeEffect.ShowMessage(
                        throwable.toAppError().toUiText(
                            defaultMessage = UiText.StringResource(R.string.error_failed_load_reminders)
                        )
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNextReminder() {
        dateTimeTicker.nowMinuteFlow().flatMapLatest { now ->
                observeNextReminderOccurrenceUseCase(
                    date = now
                )
            }.distinctUntilChanged()
                .onEach { occurrence ->
                    updateState { state ->
                        state.copy(
                            nextOccurrence = occurrence
                        )
                    }
                }
                .catch { throwable ->
                    errorReporter.record(throwable)
                    sendEffect(
                        HomeEffect.ShowMessage(
                            throwable.toAppError().toUiText(
                                defaultMessage = UiText.StringResource(R.string.error_failed_load_reminders)
                            )
                        )
                    )
                }
                .launchIn(viewModelScope)
    }

    private fun observeWorkSession(){
        observeWorkSessionStateUseCase().onEach { state ->
            updateState {
                it.copy(
                    isSessionActive = state.isActive,
                    sessionStartedAtMillis = state.startedAtMillis,
                    sessionReminderCount = state.sessionReminderCount
                )
            }
        }.catch { throwable ->
            errorReporter.record(throwable)
            sendEffect(
                HomeEffect.ShowMessage(
                    throwable.toAppError().toUiText(
                        defaultMessage = UiText.StringResource(R.string.error_failed_load_reminders)
                    )
                )
            )
        }.launchIn(viewModelScope)
    }

    private fun restoreActiveWorkSessionRuntime() {
        viewModelScope.launch {
            restoreActiveWorkSessionRuntimeUseCase().toUiMessageOrNull()?.let { message ->
                sendEffect(HomeEffect.ShowMessage(message))
            }
        }
    }

    private fun toggleWorkSession(){
        viewModelScope.launch {
            if (currentState.isSessionActionInProgress) return@launch

            updateState { it.copy(isSessionActionInProgress = true) }

            try {
                val result = runSuspendCatching(errorReporter) {
                    toggleWorkSessionUseCase()
                }.getOrElse { throwable ->
                    ToggleWorkSessionResult.StartFailed(throwable.toAppError())
                }

                handleToggleWorkSessionResult(result)
            } finally {
                updateState { it.copy(isSessionActionInProgress = false) }
            }
        }
    }

    private fun completeOccurrence(
        occurrence: ReminderOccurrence
    ) {
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                reminderCommandUseCase.completeOccurrence(occurrence)
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                sendEffect(
                    HomeEffect.ShowMessage(message)
                )
            }
        }
    }

    private fun skipOccurrence(
        occurrence: ReminderOccurrence
    ) {
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                reminderCommandUseCase.skipOccurrence(occurrence)
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                sendEffect(
                    HomeEffect.ShowMessage(message)
                )
            }
        }
    }

    private fun buildGreeting(time: LocalTime): UiText {
        val hour = time.hour

        return when (hour) {
            in 5..11 -> UiText.StringResource(R.string.greeting_morning)
            in 12..16 -> UiText.StringResource(R.string.greeting_afternoon)
            in 17..21 -> UiText.StringResource(R.string.greeting_evening)
            else -> UiText.StringResource(R.string.greeting_night)
        }
    }

    private fun buildDateText(date: LocalDate): String {
        val locale = Locale.getDefault()
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val dateText = date.format(DateTimeFormatter.ofPattern("dd MMMM y", locale))
        return "$dayOfWeek, $dateText"
    }

    private fun handleToggleWorkSessionResult(
        result: ToggleWorkSessionResult
    ) {
        sendEffect(HomeEffect.ShowMessage(result.toUiMessage()))
    }
}
