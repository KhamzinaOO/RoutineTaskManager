package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.onErrorMessage
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.error.toAppError
import com.okhamzina.routinetaskmanager.core.error.toUiText
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.featureReminder.application.command.ReminderCommandUseCase
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.type
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersEffect
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersUiState
import com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderFilter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllRemindersViewModel(
    private val remindersCommand : ReminderCommandUseCase,
    private val errorReporter: ErrorReporter
) : ViewModel() {

    private val _effect = Channel<AllRemindersEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(AllRemindersUiState())
    val uiState: StateFlow<AllRemindersUiState> = _uiState.asStateFlow()

    init {
        observeReminders()
    }

    fun onIntent(intent: AllRemindersIntent) {
        when (intent) {
            AllRemindersIntent.OnAddFABClick -> {
                sendEffect(AllRemindersEffect.FABClicked)
            }

            is AllRemindersIntent.OnItemClick -> {
                sendEffect(AllRemindersEffect.ItemClicked(intent.id))
            }

            AllRemindersIntent.OnMenuButtonClick -> {
                sendEffect(AllRemindersEffect.MenuButtonClicked)
            }

            is AllRemindersIntent.OnOpenClick -> {
                sendEffect(AllRemindersEffect.OpenClicked(intent.id))
            }

            is AllRemindersIntent.OnEditClick -> {
                sendEffect(AllRemindersEffect.EditClicked(intent.id))
            }

            is AllRemindersIntent.OnDeleteClick -> {
                deleteReminder(intent.id)
            }

            is AllRemindersIntent.ShowMessage -> {
                sendEffect(AllRemindersEffect.ShowMessage(UiText.DynamicString(intent.message)))
            }

            is AllRemindersIntent.FilterReminder -> {
                updateFilter(intent.filter)
            }

            is AllRemindersIntent.OnTypeSelected -> {
                val selectedType = _uiState.value.repeatTypeFilterList
                    .find { it.id == intent.typeIndex }

                updateFilter(
                    _uiState.value.reminderFilter.copy(
                        repeatType = selectedType?.repeatType
                    )
                )
            }
        }
    }

    private fun observeReminders() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            remindersCommand.observeReminders()
                .catch { throwable ->
                    _uiState.update { state ->
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
                    _uiState.update { state ->
                        val newState = state.copy(
                            reminders = reminders,
                            isLoading = false
                        )

                        newState.copy(
                            remindersToShow = newState.filteredReminders()
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
        _uiState.update { state ->
            val newState = state.copy(
                reminderFilter = filter
            )

            newState.copy(
                remindersToShow = newState.filteredReminders()
            )
        }
    }

    private fun AllRemindersUiState.filteredReminders() =
        reminders.filter { reminder ->
            val typeMatches =
                reminderFilter.repeatType == null ||
                        reminder.repeatRule.type == reminderFilter.repeatType

            val searchMatches =
                reminderFilter.searchText.isBlank() ||
                        reminder.name.contains(reminderFilter.searchText, ignoreCase = true)

            typeMatches && searchMatches
        }

    private fun sendEffect(effect: AllRemindersEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
