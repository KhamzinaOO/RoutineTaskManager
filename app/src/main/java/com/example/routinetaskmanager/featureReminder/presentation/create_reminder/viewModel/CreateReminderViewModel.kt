package com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.domain.useCase.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.parseHourMinuteOrNull
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toRepeatRule
import com.example.routinetaskmanager.featureReminder.presentation.common.model.AfterAnotherRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.TimeWindowUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

class CreateReminderViewModel(
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReminderUiState())
    val uiState: StateFlow<CreateReminderUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreateReminderEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: CreateReminderIntent) {
        when (intent) {
            is CreateReminderIntent.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.InstructionsChanged -> {
                _uiState.update {
                    it.copy(
                        instructions = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.TakePictureClicked -> {
                sendEffect(CreateReminderEffect.OpenImagePicker)
            }

            is CreateReminderIntent.RepeatTypeChanged -> {
                _uiState.update {
                    it.copy(
                        repeatType = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.AfterAnotherStateChanged -> {
                _uiState.update {
                    it.copy(
                        afterAnotherState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.OnSchedulePeriodStateChanged -> {
                _uiState.update {
                    it.copy(
                        onSchedulePeriodState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.OnScheduleCertainStateChanged -> {
                _uiState.update {
                    it.copy(
                        onScheduleCertainState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.DuringSessionStateChanged -> {
                _uiState.update {
                    it.copy(
                        duringSessionState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.NotificationModeChanged -> {
                _uiState.update {
                    it.copy(
                        notificationMode = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.ImageAdded -> {
                _uiState.update {
                    it.copy(
                        imageUris = it.imageUris + intent.uri,
                        errorMessage = null
                    )
                }
            }

            is CreateReminderIntent.ImageRemoved -> {
                _uiState.update {
                    it.copy(
                        imageUris = it.imageUris - intent.uri,
                        errorMessage = null
                    )
                }
            }

            CreateReminderIntent.SaveClicked -> {
                saveReminder()
            }

            CreateReminderIntent.BackClicked -> {
                sendEffect(CreateReminderEffect.NavigateBack)
            }

            CreateReminderIntent.ErrorShown -> {
                _uiState.update {
                    it.copy(errorMessage = null)
                }
            }
        }
    }

    private fun saveReminder() {
        val state = _uiState.value

        if (state.isSaving) return
//        val validationError = validateState(state)
//        if (validationError != null) {
//            _uiState.update {
//                it.copy(errorMessage = validationError)
//            }
//            sendEffect(CreateReminderEffect.ShowMessage(validationError))
//            return
//        }

        val repeatRule = buildRepeatRule(state)

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true)
            }

            runCatching {
                reminderRepository.createReminder(
                    name = state.name.trim(),
                    instructionsText = state.instructions
                        .trim()
                        .takeIf { it.isNotBlank() },
                    repeatRule = repeatRule,
                    notificationMode = state.notificationMode,
                    imageUris = state.imageUris
                )
                rescheduleRemindersUseCase.invoke()
            }.onSuccess {
                _uiState.update {
                    it.copy(isSaving = false)
                }

                _effect.send(CreateReminderEffect.NavigateBack)
            }.onFailure { throwable ->
                val message = throwable.message ?: "Failed to create reminder"

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = message
                    )
                }

                _effect.send(CreateReminderEffect.ShowMessage(message))
            }
        }
    }

    private fun validateState(
        state: CreateReminderUiState
    ): String? {
        if (state.name.isBlank()) {
            return "Enter reminder name"
        }

        return when (state.repeatType) {
            ReminderRepeatType.ON_SCHEDULE_PERIOD -> {
                validateOnSchedulePeriod(state.onSchedulePeriodState)
            }

            ReminderRepeatType.ON_SCHEDULE_CERTAIN -> {
                validateOnScheduleCertain(state.onScheduleCertainState)
            }

            ReminderRepeatType.DURING_SESSION_PERIOD -> {
                validateDuringSession(state.duringSessionState)
            }

            ReminderRepeatType.AFTER_ANOTHER_ACTIVITY -> {
                validateAfterAnother(state.afterAnotherState)
            }
        }
    }

    private fun buildRepeatRule(
        state: CreateReminderUiState
    ): ReminderRepeatRule {
        return when (state.repeatType) {
            ReminderRepeatType.ON_SCHEDULE_PERIOD -> {
                state.onSchedulePeriodState.toRepeatRule()
            }

            ReminderRepeatType.ON_SCHEDULE_CERTAIN -> {
                state.onScheduleCertainState.toRepeatRule()
            }

            ReminderRepeatType.DURING_SESSION_PERIOD -> {
                state.duringSessionState.toRepeatRule()
            }

            ReminderRepeatType.AFTER_ANOTHER_ACTIVITY -> {
                state.afterAnotherState.toRepeatRule()
            }
        }
    }

    private fun sendEffect(
        effect: CreateReminderEffect
    ) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun validateAfterAnother(
        state: AfterAnotherRepeatUi
    ): String? {
        return validateRepeatInterval(state.waitInterval)
    }

    private fun validateDuringSession(
        state: DuringSessionPeriodRepeatUi
    ): String? {
        return validateWeeklyRepeat(
            schedule = state.schedule,
            valueValidator = { intervalRepeat ->
                validateRepeatInterval(intervalRepeat.interval)
            }
        )
    }

    private fun validateOnSchedulePeriod(
        state: OnSchedulePeriodRepeatUi
    ): String? {
        return validateWeeklyRepeat(
            schedule = state.schedule,
            valueValidator = { day ->
                validateRepeatInterval(day.interval)
                    ?: validateTimeWindow(day.timeWindow)
            }
        )
    }

    private fun validateOnScheduleCertain(
        state: OnScheduleCertainRepeatUi
    ): String? {
        return validateWeeklyRepeat(
            schedule = state.schedule,
            valueValidator = { day ->
                validateCertainTime(day)
            }
        )
    }

    private fun validateRepeatInterval(
        interval: RepeatIntervalUi
    ): String? {
        val value = interval.value.trim().toIntOrNull()

        if (value == null) {
            return "Repeat interval must be a number"
        }

        if (value <= 0) {
            return "Repeat interval must be greater than zero"
        }

        return null
    }

    private fun validateTimeWindow(
        timeWindow: TimeWindowUi
    ): String? {
        if (timeWindow.allDayEnabled) {
            return null
        }

        val start = runCatching {
            LocalTime.parse(timeWindow.startTime)
        }.getOrNull()

        val end = runCatching {
            LocalTime.parse(timeWindow.endTime)
        }.getOrNull()

        if (start == null || end == null) {
            return "Time window must be valid"
        }

        if (!start.isBefore(end)) {
            return "Start time must be before end time"
        }

        return null
    }

    private fun validateCertainTime(
        day: OnScheduleCertainDayUi
    ): String? {
        val typedTime = parseHourMinuteOrNull(
            hours = day.hours,
            minutes = day.minutes
        )

        if (day.pickedTimes.isEmpty() && typedTime == null) {
            return "Add at least one valid time"
        }

        return null
    }

    private fun <T> validateWeeklyRepeat(
        schedule: WeeklyRepeatUi<T>,
        valueValidator: (T) -> String?
    ): String? {
        if (schedule.selectedDays.isEmpty()) {
            return "Select at least one day"
        }

        return when (schedule.mode) {
            RepeatScheduleMode.DEFAULT -> {
                valueValidator(schedule.defaultValue)
            }

            RepeatScheduleMode.ADVANCED -> {
                val enabledEntries = schedule.advancedEntries.filter { it.enabled }

                if (enabledEntries.isEmpty()) {
                    return "Enable at least one day"
                }

                enabledEntries
                    .firstNotNullOfOrNull { entry ->
                        valueValidator(entry.value)
                    }
            }
        }
    }
}