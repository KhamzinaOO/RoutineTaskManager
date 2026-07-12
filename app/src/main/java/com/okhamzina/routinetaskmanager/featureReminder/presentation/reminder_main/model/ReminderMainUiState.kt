package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import java.time.LocalDate

data class ReminderMainUiState (
    val reminders : List<ReminderOccurrence> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val isSessionActive: Boolean = false,
    val sessionStartedAtMillis: Long? = null,
    val sessionReminderCount: Int = 0,
    val isSessionActionInProgress: Boolean = false
)
