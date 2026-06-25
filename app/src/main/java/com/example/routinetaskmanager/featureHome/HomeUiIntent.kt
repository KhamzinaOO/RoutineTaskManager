package com.example.routinetaskmanager.featureHome

import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model.ReminderMainIntent
import java.time.LocalDate

sealed interface HomeUiIntent {
    data object OnSessionButtonClick : HomeUiIntent
    data object NotificationPermissionDenied : HomeUiIntent
    data class DateClick(
        val date : LocalDate
    ) : HomeUiIntent
    data object AddReminderClick : HomeUiIntent
    data object AddTaskClick : HomeUiIntent
}