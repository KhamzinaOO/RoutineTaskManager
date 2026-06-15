package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.dayRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.domain.useCase.ObserveReminderScheduleUseCase
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainEffect
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainIntent
import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainUiState
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
import java.time.LocalDate

class ReminderMainViewModel(
    private val reminderRepository: ReminderRepository,
    private val reminderUseCase : ObserveReminderScheduleUseCase
) : ViewModel() {

    private val _effect = Channel<ReminderMainEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(ReminderMainUiState())
    val uiState : StateFlow<ReminderMainUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders(){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                runCatching {
                    reminderUseCase.invoke(range = dayRange(LocalDate.now()))
                }.onSuccess {
                    it.distinctUntilChanged().collect { reminders ->
                        _uiState.update {
                            it.copy(
                                reminders = reminders
                            )
                        }
                    }
                }
            }
        }
    }

    fun onIntent(intent : ReminderMainIntent) {
        when(intent) {
            is ReminderMainIntent.AddFABClick -> TODO()
            is ReminderMainIntent.CalendarButtonClick -> TODO()
            is ReminderMainIntent.CalendarSwipe -> TODO()
            is ReminderMainIntent.DateClick -> TODO()
            is ReminderMainIntent.MenuButtonClick -> {
                sendEffect(ReminderMainEffect.OpenDrawer)
            }
            is ReminderMainIntent.SearchButtonClick -> TODO()
            is ReminderMainIntent.SessionButtonClick -> TODO()
        }
    }

    private fun sendEffect(
        effect: ReminderMainEffect
    ) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}