package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence

data class ReminderMainUiState (
    val reminders : List<ReminderOccurrence> = emptyList(),
    val isSessionActive: Boolean = false,
    val sessionStartedAtMillis: Long? = null,
    val sessionReminderCount: Int = 0
)
