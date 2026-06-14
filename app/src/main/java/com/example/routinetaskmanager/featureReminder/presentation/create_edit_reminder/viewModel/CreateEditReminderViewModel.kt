package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderSaveData
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.useCase.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.parseHourMinuteOrNull
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toRepeatRule
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toUiStateBundle
import com.example.routinetaskmanager.featureReminder.presentation.common.model.AfterAnotherRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.TimeWindowUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderEffect
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderMode
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

class CreateEditReminderViewModel(
    private val id : Long?,
    private val commandUseCase: ReminderCommandUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditReminderUiState())
    val uiState: StateFlow<CreateEditReminderUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreateEditReminderEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        if (id != null) {
            loadReminder()
        }
    }

    fun onIntent(intent: CreateEditReminderIntent) {
        when (intent) {
            is CreateEditReminderIntent.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.InstructionsChanged -> {
                _uiState.update {
                    it.copy(
                        instructions = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.TakePictureClicked -> {
                sendEffect(CreateEditReminderEffect.OpenImagePicker)
            }

            is CreateEditReminderIntent.RepeatTypeChanged -> {
                _uiState.update {
                    it.copy(
                        repeatType = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.AfterAnotherStateChanged -> {
                _uiState.update {
                    it.copy(
                        afterAnotherState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.OnSchedulePeriodStateChanged -> {
                _uiState.update {
                    it.copy(
                        onSchedulePeriodState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.OnScheduleCertainStateChanged -> {
                _uiState.update {
                    it.copy(
                        onScheduleCertainState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.DuringSessionStateChanged -> {
                _uiState.update {
                    it.copy(
                        duringSessionState = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.NotificationModeChanged -> {
                _uiState.update {
                    it.copy(
                        notificationMode = intent.value,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.ImageAdded -> {
                _uiState.update {
                    it.copy(
                        imageUris = it.imageUris + intent.uri,
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.ImageRemoved -> {
                _uiState.update {
                    it.copy(
                        imageUris = it.imageUris - intent.uri,
                        errorMessage = null
                    )
                }
            }

            CreateEditReminderIntent.SaveClicked -> {
                saveReminder()
            }

            CreateEditReminderIntent.BackClicked -> {
                sendEffect(CreateEditReminderEffect.NavigateBack)
            }

            CreateEditReminderIntent.ErrorShown -> {
                _uiState.update {
                    it.copy(errorMessage = null)
                }
            }
        }
    }

    private fun saveReminder() {
        val state = _uiState.value

        if (state.isSaving) return

        val validationError = validateState(state)
        if (validationError != null) {
            _uiState.update {
                it.copy(errorMessage = validationError)
            }
            sendEffect(CreateEditReminderEffect.ShowMessage(validationError))
            return
        }

        val repeatRule = buildRepeatRule(state)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            runCatching {
                when (state.screenMode) {
                    CreateEditReminderMode.Create -> {
                        buildSaveData(state)
                    }

                    is CreateEditReminderMode.Edit -> {
                        id?.let {
                            commandUseCase.updateReminder(
                                reminderId = it,
                                buildSaveData(state)
                            )
                        }
                    }
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                sendEffect(CreateEditReminderEffect.NavigateBack)
            }.onFailure { throwable ->
                val message = throwable.message ?: "Failed to save reminder"

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = message
                    )
                }

                sendEffect(CreateEditReminderEffect.ShowMessage(message))
            }
        }
    }

    private fun buildSaveData(
        state: CreateEditReminderUiState
    ): ReminderSaveData {
        return ReminderSaveData(
            name = state.name,
            instructionsText = state.instructions,
            repeatRule = buildRepeatRule(state),
            notificationMode = state.notificationMode,
            imageUris = state.imageUris
        )
    }

    private fun validateState(
        state: CreateEditReminderUiState
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
        state: CreateEditReminderUiState
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

    fun loadReminder(){
        viewModelScope.launch {
            runCatching {
                id?.let { commandUseCase.getReminderById(it) }
            }.onSuccess { result ->
                result?.let { reminder ->
                    val repeatUiState = reminder.repeatRule.toUiStateBundle()

                    _uiState.update {
                        it.copy(
                            name = reminder.name,
                            instructions = reminder.instructionsText ?: "",
                            repeatType = repeatUiState.repeatType,
                            afterAnotherState = repeatUiState.afterAnotherState,
                            onSchedulePeriodState = repeatUiState.onSchedulePeriodState,
                            onScheduleCertainState = repeatUiState.onScheduleCertainState,
                            duringSessionState = repeatUiState.duringSessionState,
                            notificationMode = reminder.notificationMode,
                            imageUris = reminder.images.map { image ->
                                image.imagePath.toUri()
                            }
                        )
                    }
                }
            }.onFailure { throwable ->
                val message = throwable.message ?: "Failed to load reminder"

                _effect.send(
                    CreateEditReminderEffect.ShowMessage(message)
                )
            }
        }
    }

    private fun sendEffect(
        effect: CreateEditReminderEffect
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