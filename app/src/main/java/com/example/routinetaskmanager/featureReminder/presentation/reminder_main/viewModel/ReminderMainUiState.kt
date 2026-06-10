package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence

data class ReminderMainUiState (
    val reminders : List<ReminderOccurrence> = emptyList()
)