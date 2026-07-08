package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.error.onErrorMessage
import com.example.routinetaskmanager.core.error.onSuccess
import com.example.routinetaskmanager.core.error.runAppCatching
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderDraft
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderImageInput
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toRepeatRule
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toUiStateBundle
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.CreateEditReminderValidator
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderEffect
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderMode
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.ReminderImageUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            _uiState.update {
                it.copy(
                    screenMode = CreateEditReminderMode.Edit(id)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    screenMode = CreateEditReminderMode.Create
                )
            }
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
                        images = it.images + ReminderImageUi.Picked(
                            uriString = intent.path
                        ),
                        errorMessage = null
                    )
                }
            }

            is CreateEditReminderIntent.ImageRemoved -> {
                _uiState.update { state ->
                    state.copy(
                        images = state.images.filterNot { image ->
                            image.key == intent.key
                        },
                        errorMessage = null
                    )
                }
            }

            CreateEditReminderIntent.SaveClicked -> {
                saveReminder()
            }

            CreateEditReminderIntent.NotificationPermissionGranted -> {
                rescheduleNotificationsAfterPermissionGrant()
            }

            CreateEditReminderIntent.ExactAlarmPermissionDenied -> {
                sendEffect(
                    CreateEditReminderEffect.ShowMessage(
                        UiText.StringResource(R.string.error_exact_alarm_permission_denied)
                    )
                )
                rescheduleNotificationsAfterPermissionGrant()
            }

            CreateEditReminderIntent.NotificationPermissionDenied -> {
                sendEffect(
                    CreateEditReminderEffect.ShowMessage(
                        UiText.StringResource(R.string.notifications_disabled_saved_without_alerts)
                    )
                )
                sendEffect(CreateEditReminderEffect.NavigateBack)
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

        val validationError = CreateEditReminderValidator.validate(state)
        if (validationError != null) {
            _uiState.update {
                it.copy(errorMessage = validationError)
            }
            sendEffect(CreateEditReminderEffect.ShowMessage(validationError))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            runAppCatching {
                when (state.screenMode) {
                    is CreateEditReminderMode.Create -> {
                        commandUseCase.createReminder(
                            buildDraft(state)
                        )
                    }

                    is CreateEditReminderMode.Edit -> {
                        id?.let {
                            commandUseCase.updateReminder(
                                reminderId = it,
                                buildDraft(state)
                            )
                        }
                    }
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }

                when (state.screenMode) {
                    is CreateEditReminderMode.Create -> {
                        sendEffect(CreateEditReminderEffect.RequestNotificationPermission)
                    }

                    is CreateEditReminderMode.Edit -> {
                        sendEffect(CreateEditReminderEffect.NavigateBack)
                    }
                }
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_save_reminder)
            ) { message ->
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

    private fun rescheduleNotificationsAfterPermissionGrant() {
        viewModelScope.launch {
            runAppCatching {
                commandUseCase.rescheduleReminderNotifications()
            }.onSuccess {
                sendEffect(CreateEditReminderEffect.NavigateBack)
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_schedule_reminder_notifications)
            ) { message ->
                sendEffect(
                    CreateEditReminderEffect.ShowMessage(message)
                )
                sendEffect(CreateEditReminderEffect.NavigateBack)
            }
        }
    }

    private fun buildDraft(
        state: CreateEditReminderUiState
    ): ReminderDraft {
        return ReminderDraft(
            name = state.name,
            instructionsText = state.instructions,
            repeatRule = buildRepeatRule(state),
            notificationMode = state.notificationMode,
            images = state.images.map { image ->
                if (image is ReminderImageUi.Saved) {
                    ReminderImageInput.Existing(
                        id = image.id,
                        path = image.path,
                        sortOrder = image.sortOrder
                    )
                } else {
                    ReminderImageInput.NewExternal(uriString = (image as ReminderImageUi.Picked).uriString)
                }
            }
        )
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
        }
    }

    fun loadReminder(){
        viewModelScope.launch {
            runAppCatching {
                id?.let { commandUseCase.getReminderById(it) }
            }.onSuccess { result ->
                result?.let { reminder ->
                    val repeatUiState = reminder.repeatRule.toUiStateBundle()

                    _uiState.update {
                        it.copy(
                            id = id,
                            name = reminder.name,
                            instructions = reminder.instructionsText ?: "",
                            repeatType = repeatUiState.repeatType,
                            onSchedulePeriodState = repeatUiState.onSchedulePeriodState,
                            onScheduleCertainState = repeatUiState.onScheduleCertainState,
                            duringSessionState = repeatUiState.duringSessionState,
                            notificationMode = reminder.notificationMode,
                            images = reminder.images.map { image ->
                                ReminderImageUi.Saved(
                                    id = image.id,
                                    path = image.imagePath,
                                    sortOrder = image.sortOrder
                                )
                            }
                        )
                    }
                }
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_load_reminder)
            ) { message ->
                sendEffect(CreateEditReminderEffect.ShowMessage(message))
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

}
