package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel

import java.time.LocalDate

sealed interface ReminderMainIntent {
    data object MenuButtonClick : ReminderMainIntent
    data object SearchButtonClick : ReminderMainIntent
    data object CalendarButtonClick : ReminderMainIntent
    data object CalendarSwipe : ReminderMainIntent
    data class DateClick(
        val date : LocalDate
    ) : ReminderMainIntent
    data object SessionButtonClick : ReminderMainIntent
    data object AddFABClick : ReminderMainIntent

}
