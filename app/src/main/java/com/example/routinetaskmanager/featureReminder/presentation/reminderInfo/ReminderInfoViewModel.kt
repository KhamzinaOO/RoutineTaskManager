package com.example.routinetaskmanager.featureReminder.presentation.reminderInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.error.toAppError
import com.example.routinetaskmanager.core.error.toUiText
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.core.utills.formatDateTimeToWeekDayAndTime
import com.example.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.example.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceByIdUseCase
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoEffect
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoIntent
import com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoUiState
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReminderInfoViewModel(
    private val reminderId : Long,
    private val commandUseCase : ReminderCommandUseCase,
    private val observeNextReminderOccurrenceById: ObserveNextReminderOccurrenceByIdUseCase
) : ViewModel(){
    private val _uiState = MutableStateFlow<ReminderInfoUiState>(ReminderInfoUiState())
    val uiState : StateFlow<ReminderInfoUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ReminderInfoEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: ReminderInfoIntent){
        when(intent){
            is ReminderInfoIntent.OnReminderDelete -> {
                deleteReminder()
            }
            is ReminderInfoIntent.OnReminderEdit -> {
               sendEffect(ReminderInfoEffect.EditReminder(reminderId))
            }

            is ReminderInfoIntent.OnDoButtonClick -> {
                completeNextOccurrence()
            }

            is ReminderInfoIntent.OnSkipButtonClick -> {
                skipNextOccurrence()
            }

            is ReminderInfoIntent.OnSkipAllForTodayClick -> {
                skipRemainingForToday()
            }
            is ReminderInfoIntent.OnSetEnabled -> {
                setReminderEnabled(intent.enabled)
            }
            is ReminderInfoIntent.OnBackClick ->{
                sendEffect(ReminderInfoEffect.NavigateBack)
            }
        }
    }

    init {
        loadReminder()
        getNextReminder()
    }

    private fun completeNextOccurrence() {
        val occurrence = uiState.value.nextOccurrence ?: return

        viewModelScope.launch {
            runCatching {
                commandUseCase.completeOccurrence(
                    occurrence = occurrence
                )
            }.onFailure { error ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(
                        error.toAppError().toUiText(
                            defaultMessage = UiText.StringResource(
                                R.string.error_failed_update_reminder_status
                            )
                        )
                    )
                )
            }
        }
    }

    private fun skipNextOccurrence() {
        val occurrence = uiState.value.nextOccurrence ?: return

        viewModelScope.launch {
            runCatching {
                commandUseCase.skipOccurrence(
                    occurrence = occurrence
                )
            }.onFailure { error ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(
                        error.toAppError().toUiText(
                            defaultMessage = UiText.StringResource(
                                R.string.error_failed_update_reminder_status
                            )
                        )
                    )
                )
            }
        }
    }

    private fun skipRemainingForToday() {
        viewModelScope.launch {
            runCatching {
                commandUseCase.skipRemainingForToday(
                    reminderId = reminderId,
                    date = LocalDate.now()
                )
            }.onFailure { error ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(
                        error.toAppError().toUiText(
                            defaultMessage = UiText.StringResource(
                                R.string.error_failed_update_reminder_status
                            )
                        )
                    )
                )
            }
        }
    }

    private fun loadReminder(){
        viewModelScope.launch {
            runCatching {
               commandUseCase.observeReminderById(reminderId).collectLatest { result ->
                   _uiState.update {
                       it.copy(
                           reminder = result
                       )
                   }
               }
            }.onFailure { error ->
                _effect.send(ReminderInfoEffect.ShowMessage(
                    error.toAppError().toUiText(
                        defaultMessage = UiText.StringResource(R.string.error_failed_load_reminder)
                    )
                ))
            }
        }
    }

    private fun deleteReminder(){
        viewModelScope.launch {
            runCatching {
                commandUseCase.deleteReminder(reminderId)
            }.onSuccess {
                _effect.send(ReminderInfoEffect.NavigateBack)
            }.onFailure { error ->
                _effect.send(ReminderInfoEffect.ShowMessage(
                    error.toAppError().toUiText(
                        defaultMessage = UiText.StringResource(R.string.error_failed_delete_reminder)
                    )
                ))
            }
        }
    }

    private fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                commandUseCase.setReminderEnabled(
                    reminderId = reminderId,
                    enabled = enabled
                )
            }.onFailure { error ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(
                        error.toAppError().toUiText(
                            defaultMessage = UiText.StringResource(
                                R.string.error_failed_update_reminder_status
                            )
                        )
                    )
                )
            }
        }
    }

    private fun getNextReminder(){
        viewModelScope.launch {
            runCatching {
                observeNextReminderOccurrenceById(
                    date = LocalDateTime.now(),
                    reminderId = reminderId
                ).distinctUntilChanged()
            }.onSuccess { occurrenceFlow ->
                occurrenceFlow.onEach { occurrence ->
                    _uiState.update {
                        it.copy(
                            nextOccurrence = occurrence
                        )
                    }
                }.catch { throwable ->
                    _effect.send(
                        ReminderInfoEffect.ShowMessage(
                            throwable.toAppError().toUiText(
                                defaultMessage = UiText.StringResource(R.string.error_failed_load_reminder)
                            )
                        )
                    )
                }.collect {}

            }
        }
    }

    private fun sendEffect(
        effect: ReminderInfoEffect
    ) {
        _effect.trySend(effect)
    }
}
