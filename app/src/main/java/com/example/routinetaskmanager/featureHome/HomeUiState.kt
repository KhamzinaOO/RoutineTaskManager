package com.example.routinetaskmanager.featureHome

import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence

data class HomeUiState (
    val greetingText : String = "",
    val dateText : String = "",
    val reminders: List<ReminderOccurrence> = emptyList(),
    val isSessionActive: Boolean = false,
    val sessionStartedAtMillis: Long? = null,
    val sessionReminderCount: Int = 0,
    val isSessionActionInProgress: Boolean = false
)
