package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import java.time.LocalDate

data class ReminderInfoUiState(
    val reminder : Reminder? = null,
    val nextOccurrence: ReminderOccurrence? = null,
    val today: LocalDate = LocalDate.now()
)
