package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence

data class ReminderInfoUiState(
    val reminder : Reminder? = null,
    val nextOccurrence: ReminderOccurrence? = null,
    val linkedReminder : Reminder? = null
)