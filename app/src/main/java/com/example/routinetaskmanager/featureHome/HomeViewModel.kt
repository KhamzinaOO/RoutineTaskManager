package com.example.routinetaskmanager.featureHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.error.onErrorMessage
import com.example.routinetaskmanager.core.error.onSuccess
import com.example.routinetaskmanager.core.error.runAppCatching
import com.example.routinetaskmanager.core.error.runSuspendCatching
import com.example.routinetaskmanager.core.error.toAppError
import com.example.routinetaskmanager.core.error.toUiText
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.core.time.DateTimeTicker
import com.example.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveDayReminderOccurrencesUseCase
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceUseCase
import com.example.routinetaskmanager.featureReminder.application.session.ObserveWorkSessionStateUseCase
import com.example.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionResult
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toUiMessage
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toUiMessageOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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
    private val dateTimeTicker: DateTimeTicker
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeTodayReminders()
        observeNextReminder()
        observeWorkSession()
        restoreActiveWorkSessionRuntime()
    }

    fun onIntent(intent : HomeUiIntent){
        when (intent) {
            is HomeUiIntent.AddReminderClick -> {
                sendEffect(HomeEffect.NavigateCreateReminder)
            }

            is HomeUiIntent.AddTaskClick -> {
                sendEffect(HomeEffect.NavigateTasks)
            }

            is HomeUiIntent.DateClick -> Unit

            is HomeUiIntent.NotificationPermissionDenied -> {
                sendEffect(
                    HomeEffect.ShowMessage(
                        UiText.StringResource(R.string.error_notification_permission_denied)
                    )
                )
            }

            is HomeUiIntent.OnNextReminderDoneClick -> {
                completeOccurrence(intent.occurrence)
            }

            is HomeUiIntent.OnNextReminderSkipClick -> {
                skipOccurrence(intent.occurrence)
            }

            is HomeUiIntent.OnSessionButtonClick -> {
                toggleWorkSession()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodayReminders() {
        dateTimeTicker.todayFlow()
            .onEach { today ->
                _uiState.update {
                    it.copy(
                        greetingText = buildGreeting(),
                        dateText = buildDateText(today)
                    )
                }
            }
            .flatMapLatest { today ->
                observeDayReminderOccurrencesUseCase(date = today)
            }
            .onEach { reminders ->
                _uiState.update {
                    it.copy(reminders = reminders)
                }
            }
            .catch { throwable ->
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
        viewModelScope.launch {
            dateTimeTicker.nowMinuteFlow().flatMapLatest { now ->
                observeNextReminderOccurrenceUseCase(
                    date = now
                )
            }.distinctUntilChanged()
                .onEach { occurrence ->
                    _uiState.update { state ->
                        state.copy(
                            nextOccurrence = occurrence
                        )
                    }
                }
                .catch { throwable ->
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
    }

    private fun observeWorkSession(){
        observeWorkSessionStateUseCase().onEach { state ->
            _uiState.update {
                it.copy(
                    isSessionActive = state.isActive,
                    sessionStartedAtMillis = state.startedAtMillis,
                    sessionReminderCount = state.sessionReminderCount
                )
            }
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
            if (_uiState.value.isSessionActionInProgress) return@launch

            _uiState.update { it.copy(isSessionActionInProgress = true) }

            val result = runSuspendCatching {
                toggleWorkSessionUseCase()
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.StartFailed(throwable)
            }

            handleToggleWorkSessionResult(result)

            _uiState.update { it.copy(isSessionActionInProgress = false) }
        }
    }

    private fun completeOccurrence(
        occurrence: ReminderOccurrence
    ) {
        viewModelScope.launch {
            runAppCatching {
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
            runAppCatching {
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


    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun buildGreeting(): UiText {
        val hour = LocalTime.now().hour

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
