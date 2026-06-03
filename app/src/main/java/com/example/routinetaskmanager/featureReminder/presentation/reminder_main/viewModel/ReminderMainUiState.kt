package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder

data class ReminderMainUiState (
    val reminders : List<Reminder> = emptyList()
)