package com.example.routinetaskmanager.featureHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.error.toAppError
import com.example.routinetaskmanager.core.error.toUiText
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveDayReminderOccurrencesUseCase
import com.example.routinetaskmanager.featureReminder.application.session.ObserveWorkSessionStateUseCase
import com.example.routinetaskmanager.featureReminder.application.session.RestoreActiveWorkSessionRuntimeUseCase
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionResult
import com.example.routinetaskmanager.featureReminder.application.session.ToggleWorkSessionUseCase
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val observeDayReminderOccurrencesUseCase: ObserveDayReminderOccurrencesUseCase,
    private val reminderCommandUseCase: ReminderCommandUseCase,
    private val observeWorkSessionStateUseCase: ObserveWorkSessionStateUseCase,
    private val restoreActiveWorkSessionRuntimeUseCase: RestoreActiveWorkSessionRuntimeUseCase,
    private val toggleWorkSessionUseCase: ToggleWorkSessionUseCase
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
        observeTodayReminders()
        observeWorkSession()
    }

    fun onIntent(intent : HomeUiIntent){
        when (intent) {
            is HomeUiIntent.AddReminderClick -> TODO()
            is HomeUiIntent.AddTaskClick -> TODO()
            is HomeUiIntent.DateClick -> TODO()
            is HomeUiIntent.NotificationPermissionDenied -> TODO()
            is HomeUiIntent.OnSessionButtonClick -> {
                toggleWorkSession()
            }
        }
    }

    private fun observeTodayReminders(){
        viewModelScope.launch {
            observeDayReminderOccurrencesUseCase(date = LocalDate.now()).onEach { reminders ->
                _uiState.update {
                    it.copy(
                        reminders = reminders
                    )
                }
            }.launchIn(viewModelScope)
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

    private fun toggleWorkSession(){
        viewModelScope.launch {
            if (_uiState.value.isSessionActionInProgress) return@launch

            _uiState.update { it.copy(isSessionActionInProgress = true) }

            val result = runCatching {
                toggleWorkSessionUseCase()
            }.getOrElse { throwable ->
                ToggleWorkSessionResult.Failed(throwable)
            }

            handleToggleWorkSessionResult(result)

            _uiState.update { it.copy(isSessionActionInProgress = false) }
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

    private fun buildDateText(): String {
        val date = LocalDate.now()
        val locale = Locale.getDefault()
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val dateText = date.format(DateTimeFormatter.ofPattern("dd MMMM y", locale))

        return "$dayOfWeek, $dateText"
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

                sendEffect(HomeEffect.ShowMessage(message))
            }

            ToggleWorkSessionResult.StartedWithoutReminders -> {
                sendEffect(
                    HomeEffect.ShowMessage(
                        UiText.StringResource(R.string.work_session_started_no_session_reminders)
                    )
                )
            }

            ToggleWorkSessionResult.Ended -> {
                sendEffect(
                    HomeEffect.ShowMessage(
                        UiText.StringResource(R.string.work_session_ended)
                    )
                )
            }

            ToggleWorkSessionResult.ForegroundStartBlocked -> {
                sendEffect(
                    HomeEffect.ShowMessage(
                        UiText.StringResource(R.string.error_failed_start_work_session_service)
                    )
                )
            }

            is ToggleWorkSessionResult.Failed -> {
                sendEffect(
                    HomeEffect.ShowMessage(
                        result.throwable.toAppError().toUiText(
                            defaultMessage = UiText.StringResource(R.string.error_failed_start_work_session)
                        )
                    )
                )
            }
        }
    }
}
