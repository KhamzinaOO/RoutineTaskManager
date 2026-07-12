package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.onErrorMessage
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.okhamzina.routinetaskmanager.core.presentation.model.MviViewModel
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersUiState
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderFilter
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AllRemindersViewModel(
    private val remindersCommand : ReminderCommandUseCase,
    private val errorReporter: ErrorReporter
) : MviViewModel<AllRemindersUiState, AllRemindersIntent, AllRemindersEffect>(
    AllRemindersUiState()
) {

    init {
        observeReminders()
    }

    override fun onIntent(intent: AllRemindersIntent) {
        when (intent) {
            AllRemindersIntent.AddReminderClicked -> {
                sendEffect(AllRemindersEffect.NavigateToCreateReminder)
            }

            is AllRemindersIntent.ReminderClicked -> {
                sendEffect(AllRemindersEffect.NavigateToReminder(intent.id))
            }

            AllRemindersIntent.MenuButtonClicked -> {
                sendEffect(AllRemindersEffect.OpenDrawer)
            }

            AllRemindersIntent.SearchButtonClicked -> {
                sendEffect(AllRemindersEffect.OpenSearch)
            }

            is AllRemindersIntent.EditReminderClicked -> {
                sendEffect(AllRemindersEffect.NavigateToEditReminder(intent.id))
            }

            is AllRemindersIntent.DeleteReminderClicked -> {
                deleteReminder(intent.id)
            }

            is AllRemindersIntent.FilterChanged -> {
                updateFilter(intent.filter)
            }

            is AllRemindersIntent.TypeFilterSelected -> {
                val selectedType = currentState.repeatTypeFilterList
                    .find { it.id == intent.typeId }

                updateFilter(
                    currentState.reminderFilter.copy(
                        repeatType = selectedType?.repeatType
                    )
                )
            }
        }
    }

    private fun observeReminders() {
        viewModelScope.launch {
            updateState {
                it.copy(isLoading = true)
            }

            remindersCommand.observeReminders()
                .catch { throwable ->
                    updateState { state ->
                        state.copy(isLoading = false)
                    }
                    sendEffect(
                        AllRemindersEffect.ShowMessage(
                            throwable.toAppError().toUiText(
                                defaultMessage = UiText.StringResource(R.string.error_failed_load_reminders)
                            )
                        )
                    )
                }
                .collect { reminders ->
                    updateState { state ->
                        state.copy(
                            reminders = reminders,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun deleteReminder(id: Long) {
        viewModelScope.launch {
            runAppResultCatching(errorReporter) {
                remindersCommand.deleteReminder(id)
            }.onErrorMessage(
                defaultMessage = UiText.StringResource(R.string.error_failed_delete_reminder)
            ) { message ->
                sendEffect(
                    AllRemindersEffect.ShowMessage(message)
                )
            }
        }
    }

    private fun updateFilter(filter: ReminderFilter) {
        updateState { state ->
            state.copy(reminderFilter = filter)
        }
    }
}
