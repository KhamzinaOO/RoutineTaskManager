package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.onErrorMessage
import com.okhamzina.routinetaskmanager.core.error.onSuccess
import com.okhamzina.routinetaskmanager.core.error.runAppCatching
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.core.time.DateTimeTicker
import com.okhamzina.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceByIdUseCase
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderInfoViewModel(
    private val reminderId : Long,
    private val commandUseCase : ReminderCommandUseCase,
    private val observeNextReminderOccurrenceById: ObserveNextReminderOccurrenceByIdUseCase,
    private val dateTimeTicker: DateTimeTicker,
    private val errorReporter: ErrorReporter
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
            runAppResultCatching(errorReporter) {
                commandUseCase.completeOccurrence(
                    occurrence = occurrence
                )
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(message)
                )
            }
        }
    }

    private fun skipNextOccurrence() {
        val occurrence = uiState.value.nextOccurrence ?: return

        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.skipOccurrence(
                    occurrence = occurrence
                )
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(message)
                )
            }
        }
    }

    private fun skipRemainingForToday() {
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.skipRemainingForToday(
                    reminderId = reminderId,
                    date = LocalDate.now()
                )
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(message)
                )
            }
        }
    }

    private fun loadReminder(){
        viewModelScope.launch {
            runAppCatching(errorReporter) {
               commandUseCase.observeReminderById(reminderId).collectLatest { result ->
                   _uiState.update {
                       it.copy(
                           reminder = result
                       )
                   }
               }
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_load_reminder)
            ) { message ->
                _effect.send(ReminderInfoEffect.ShowMessage(
                    message
                ))
            }
        }
    }

    private fun deleteReminder(){
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.deleteReminder(reminderId)
            }.onSuccess {
                _effect.send(ReminderInfoEffect.NavigateBack)
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_delete_reminder)
            ) { message ->
                _effect.send(ReminderInfoEffect.ShowMessage(
                    message
                ))
            }
        }
    }

    private fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.setReminderEnabled(
                    reminderId = reminderId,
                    enabled = enabled
                )
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                _effect.send(
                    ReminderInfoEffect.ShowMessage(message)
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNextReminder(){
        viewModelScope.launch {
            dateTimeTicker.nowMinuteFlow().flatMapLatest{ now ->
                observeNextReminderOccurrenceById(
                    date = now,
                    reminderId = reminderId
                )
            }.distinctUntilChanged()
                .onEach {  occurrence ->
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

    private fun sendEffect(
        effect: ReminderInfoEffect
    ) {
        _effect.trySend(effect)
    }
}
