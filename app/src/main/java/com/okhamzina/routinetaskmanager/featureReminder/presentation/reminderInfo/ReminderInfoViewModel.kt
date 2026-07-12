package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo

import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.onErrorMessage
import com.okhamzina.routinetaskmanager.core.error.onSuccess
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.core.presentation.model.MviViewModel
import com.okhamzina.routinetaskmanager.core.time.DateTimeTicker
import com.okhamzina.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.schedule.ObserveNextReminderOccurrenceByIdUseCase
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model.ReminderInfoUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ReminderInfoViewModel(
    private val reminderId : Long,
    private val commandUseCase : ReminderCommandUseCase,
    private val observeNextReminderOccurrenceById: ObserveNextReminderOccurrenceByIdUseCase,
    private val dateTimeTicker: DateTimeTicker,
    private val errorReporter: ErrorReporter
) : MviViewModel<ReminderInfoUiState, ReminderInfoIntent, ReminderInfoEffect>(ReminderInfoUiState()) {
    override fun onIntent(intent: ReminderInfoIntent) {
        when(intent){
            ReminderInfoIntent.DeleteClicked -> {
                deleteReminder()
            }
            ReminderInfoIntent.EditClicked -> {
               sendEffect(ReminderInfoEffect.NavigateToEditReminder(reminderId))
            }

            ReminderInfoIntent.CompleteNextClicked -> {
                completeNextOccurrence()
            }

            ReminderInfoIntent.SkipNextClicked -> {
                skipNextOccurrence()
            }

            ReminderInfoIntent.SkipRemainingTodayClicked -> {
                skipRemainingForToday()
            }
            is ReminderInfoIntent.EnabledChanged -> {
                setReminderEnabled(intent.enabled)
            }
            ReminderInfoIntent.BackClicked ->{
                sendEffect(ReminderInfoEffect.NavigateBack)
            }
        }
    }

    init {
        loadReminder()
        getNextReminder()
    }

    private fun completeNextOccurrence() {
        val occurrence = currentState.nextOccurrence ?: return

        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.completeOccurrence(
                    occurrence = occurrence
                )
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                sendEffect(ReminderInfoEffect.ShowMessage(message))
            }
        }
    }

    private fun skipNextOccurrence() {
        val occurrence = currentState.nextOccurrence ?: return

        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.skipOccurrence(
                    occurrence = occurrence
                )
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                sendEffect(ReminderInfoEffect.ShowMessage(message))
            }
        }
    }

    private fun skipRemainingForToday() {
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.skipRemainingForToday(
                    reminderId = reminderId,
                    date = currentState.today
                )
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_update_reminder_status)
            ) { message ->
                sendEffect(ReminderInfoEffect.ShowMessage(message))
            }
        }
    }

    private fun loadReminder(){
        commandUseCase.observeReminderById(reminderId)
            .onEach { reminder ->
                updateState { state -> state.copy(reminder = reminder) }
            }
            .catch { throwable ->
                errorReporter.record(throwable)
                sendEffect(
                    ReminderInfoEffect.ShowMessage(
                        throwable.toAppError().toUiText(
                            defaultMessage = UiText.StringResource(R.string.error_failed_load_reminder)
                        )
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    private fun deleteReminder(){
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                commandUseCase.deleteReminder(reminderId)
            }.onSuccess {
                sendEffect(ReminderInfoEffect.NavigateBack)
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_delete_reminder)
            ) { message ->
                sendEffect(ReminderInfoEffect.ShowMessage(message))
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
                sendEffect(ReminderInfoEffect.ShowMessage(message))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getNextReminder(){
        viewModelScope.launch {
            dateTimeTicker.nowMinuteFlow().flatMapLatest{ now ->
                updateState { state -> state.copy(today = now.toLocalDate()) }
                observeNextReminderOccurrenceById(
                    date = now,
                    reminderId = reminderId
                )
            }.distinctUntilChanged()
                .onEach {  occurrence ->
                    updateState {
                        it.copy(
                            nextOccurrence = occurrence
                        )
                    }
            }.catch { throwable ->
                    errorReporter.record(throwable)
                    sendEffect(
                        ReminderInfoEffect.ShowMessage(
                            throwable.toAppError().toUiText(
                                defaultMessage = UiText.StringResource(R.string.error_failed_load_reminder)
                            )
                        )
                    )
                }.launchIn(this)
        }
    }

}
