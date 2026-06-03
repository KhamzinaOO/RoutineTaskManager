package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.viewModel

import com.example.routinetaskmanager.featureReminder.presentation.reminder_main.viewModel.ReminderMainIntent
import java.time.LocalDate

interface AllRemindersIntent {
    data object OnMenuButtonClick : AllRemindersIntent
    data object OnItemClick : AllRemindersIntent
    data object OnAddFABClick : AllRemindersIntent
}