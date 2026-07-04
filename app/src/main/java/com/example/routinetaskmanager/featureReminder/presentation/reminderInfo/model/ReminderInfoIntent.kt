package com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model

sealed interface ReminderInfoIntent {
    data object OnReminderDelete  : ReminderInfoIntent
    data object OnReminderEdit : ReminderInfoIntent

    data object OnSkipButtonClick : ReminderInfoIntent
    data object OnDoButtonClick : ReminderInfoIntent
    data object OnSkipAllForTodayClick : ReminderInfoIntent

    data object OnBackClick: ReminderInfoIntent
}