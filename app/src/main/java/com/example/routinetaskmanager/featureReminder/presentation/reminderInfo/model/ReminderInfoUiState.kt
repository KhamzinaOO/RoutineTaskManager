package com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence

data class ReminderInfoUiState(
    val reminder : Reminder? = null,
    val nextOccurrence: ReminderOccurrence? = null,
    val linkedReminder : Reminder? = null
)