package com.example.routinetaskmanager.featureHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.featureReminder.data.session.WorkSessionForegroundController
import com.example.routinetaskmanager.featureReminder.data.session.WorkSessionForegroundStartResult
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveReminderScheduleUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager
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
                val foregroundStarted = sessionState.startedAtMillis
                    ?.let { startedAtMillis ->
                        handleForegroundServiceStart(
                            startedAtMillis = startedAtMillis,
                            rollbackSessionOnFailure = true
                        )
                    }
                    ?: true

                if (!foregroundStarted) {
                    return@onSuccess
                }

                val message = when {
                    sessionState.scheduledNotificationCount == 0 -> {
                        UiText.StringResource(R.string.work_session_started_no_session_reminders)
                    }

                    wasActive -> {
                        UiText.PluralResource(
                            R.plurals.work_session_restarted_count,
                            sessionState.scheduledNotificationCount
                        )
                    }

                    else -> {
                        UiText.PluralResource(
                            R.plurals.work_session_started_scheduled_count,
                            sessionState.scheduledNotificationCount
                        )
                    }
                }

                sendEffect(HomeEffect.ShowMessage(message))
            }.onFailure { throwable ->
                sendEffect(
                    HomeEffect.ShowMessage(
                        throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.error_failed_start_work_session)
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
                sendEffect(HomeEffect.ShowMessage(UiText.StringResource(R.string.work_session_ended)))
            }.onFailure { throwable ->
                sendEffect(
                    HomeEffect.ShowMessage(
                        throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.error_failed_end_work_session)
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
                UiText.StringResource(R.string.notifications_disabled_enable_settings)
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
                viewModelScope.launch {
                    handleForegroundServiceStart(
                        startedAtMillis = startedAtMillis,
                        rollbackSessionOnFailure = false
                    )
                }
            }
        }
    }

    private suspend fun handleForegroundServiceStart(
        startedAtMillis: Long,
        rollbackSessionOnFailure: Boolean
    ): Boolean {
        return when (workSessionForegroundController.start(startedAtMillis)) {
            WorkSessionForegroundStartResult.Started -> true
            is WorkSessionForegroundStartResult.Failed -> {
                if (rollbackSessionOnFailure) {
                    runCatching {
                        reminderCommandUseCase.endWorkSession()
                    }
                }

                sendEffect(
                    HomeEffect.ShowMessage(
                        UiText.StringResource(
                            if (rollbackSessionOnFailure) {
                                R.string.error_failed_start_work_session_service
                            } else {
                                R.string.error_failed_restore_work_session_service
                            }
                        )
                    )
                )
                false
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
                        throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.error_failed_load_reminders)
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
}

sealed interface HomeEffect {
    data class ShowMessage(val message: UiText) : HomeEffect
}
