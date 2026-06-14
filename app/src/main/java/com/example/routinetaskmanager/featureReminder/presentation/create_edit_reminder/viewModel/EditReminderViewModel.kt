package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.featureReminder.domain.useCase.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.presentation.common.mappers.toUiStateBundle
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderEffect
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditReminderViewModel(
    private val commandUseCase : ReminderCommandUseCase,
    private val id : Long
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateEditReminderUiState())
    val uiState : StateFlow<CreateEditReminderUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreateEditReminderEffect>(Channel.BUFFERED)
    val effect : Flow<CreateEditReminderEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: CreateEditReminderIntent){
        when (intent){
            is CreateEditReminderIntent.AfterAnotherStateChanged -> TODO()
            CreateEditReminderIntent.BackClicked -> TODO()
            is CreateEditReminderIntent.DuringSessionStateChanged -> TODO()
            CreateEditReminderIntent.ErrorShown -> TODO()
            is CreateEditReminderIntent.ImageAdded -> TODO()
            is CreateEditReminderIntent.ImageRemoved -> TODO()
            is CreateEditReminderIntent.InstructionsChanged -> TODO()
            is CreateEditReminderIntent.NameChanged -> TODO()
            is CreateEditReminderIntent.NotificationModeChanged -> TODO()
            is CreateEditReminderIntent.OnScheduleCertainStateChanged -> TODO()
            is CreateEditReminderIntent.OnSchedulePeriodStateChanged -> TODO()
            is CreateEditReminderIntent.RepeatTypeChanged -> TODO()
            CreateEditReminderIntent.SaveClicked -> TODO()
            CreateEditReminderIntent.TakePictureClicked -> TODO()
        }
    }

    init {
        getReminder()
    }

    fun getReminder(){
        viewModelScope.launch {
            runCatching {
                commandUseCase.getReminderById(id)
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
}