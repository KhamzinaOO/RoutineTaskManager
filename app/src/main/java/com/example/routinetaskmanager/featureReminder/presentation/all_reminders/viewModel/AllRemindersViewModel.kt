package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.featureReminder.data.mapper.toRepeatTypeDomain
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersEffect
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersIntent
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.AllRemindersUiState
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.mapper.toMiniCardUi

class AllRemindersViewModel(
    private val reminderRepository: ReminderRepository
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
                sendEffect(AllRemindersEffect.ShowMessage(intent.message))
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

            reminderRepository.observeReminders()
                .distinctUntilChanged()
                .collect { reminders ->
                    _uiState.update { state ->
                        val newState = state.copy(
                            reminders = reminders,
                            isLoading = false
                        )

                        newState.copy(
                            remindersToShow = newState.filteredReminders().map { it.toMiniCardUi() }
                        )
                    }
                }
        }
    }

    private fun deleteReminder(id: Long) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    reminderRepository.deleteReminder(id)
                }
            }.onFailure { error ->
                sendEffect(
                    AllRemindersEffect.ShowMessage(
                        error.message ?: "Не удалось удалить напоминание"
                    )
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
                remindersToShow = newState.filteredReminders().map { it.toMiniCardUi() }
            )
        }
    }

    private fun AllRemindersUiState.filteredReminders() =
        reminders.filter { reminder ->
            val typeMatches =
                reminderFilter.repeatType == null ||
                        reminder.repeatRule.toRepeatTypeDomain() == reminderFilter.repeatType

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