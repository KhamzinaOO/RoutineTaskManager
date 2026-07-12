package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model

import java.time.LocalDate

sealed interface ReminderMainIntent {
    data object MenuButtonClicked : ReminderMainIntent
    data object SearchButtonClicked : ReminderMainIntent
    data object CalendarButtonClicked : ReminderMainIntent
    data object NotificationPermissionDenied : ReminderMainIntent
    data class DateSelected(
        val date : LocalDate
    ) : ReminderMainIntent
    data object SessionButtonClicked : ReminderMainIntent
    data object AddReminderClicked : ReminderMainIntent
}
