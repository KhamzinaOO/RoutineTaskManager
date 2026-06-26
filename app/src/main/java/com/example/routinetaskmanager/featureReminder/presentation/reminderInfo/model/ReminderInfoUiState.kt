package com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder

data class ReminderInfoUiState(
    val reminder : Reminder? = null,
    val nextReminderDateTime : String? = null,
    val linkedReminder : Reminder? = null
)