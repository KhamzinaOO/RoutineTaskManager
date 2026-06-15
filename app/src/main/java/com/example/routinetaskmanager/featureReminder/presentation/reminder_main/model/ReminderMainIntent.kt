package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model

import java.time.LocalDate

sealed interface ReminderMainIntent {
    data object MenuButtonClick : ReminderMainIntent
    data object SearchButtonClick : ReminderMainIntent
    data object CalendarButtonClick : ReminderMainIntent
    data object CalendarSwipe : ReminderMainIntent
    data object EndSessionButtonClick : ReminderMainIntent
    data object ExactAlarmAccessDenied : ReminderMainIntent
    data object NotificationPermissionDenied : ReminderMainIntent
    data class DateClick(
        val date : LocalDate
    ) : ReminderMainIntent
    data object SessionButtonClick : ReminderMainIntent
    data object AddFABClick : ReminderMainIntent

}
