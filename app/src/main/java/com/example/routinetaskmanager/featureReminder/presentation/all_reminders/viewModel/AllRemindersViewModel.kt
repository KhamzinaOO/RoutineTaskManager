package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllRemindersViewModel(
    private val reminderRepository: ReminderRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(AllRemindersUiState())
    val uiState : StateFlow<AllRemindersUiState> = _uiState.asStateFlow()

    init{
        loadReminders()
    }

    fun loadReminders(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                runCatching {
                    reminderRepository.observeReminders()
                }.onSuccess {
                    it.distinctUntilChanged()
                        .collect { reminders ->
                            _uiState.update { uiState ->
                                uiState.copy(
                                    reminders = reminders
                                )
                            }
                        }
                }
            }
        }
    }
}