package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence

data class ReminderMainUiState (
    val reminders : List<ReminderOccurrence> = emptyList()
)