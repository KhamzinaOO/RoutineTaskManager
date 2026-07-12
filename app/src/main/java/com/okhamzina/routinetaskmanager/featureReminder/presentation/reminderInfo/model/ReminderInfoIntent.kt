package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model

sealed interface ReminderInfoIntent {
    data object DeleteClicked : ReminderInfoIntent
    data object EditClicked : ReminderInfoIntent
    data object SkipNextClicked : ReminderInfoIntent
    data object CompleteNextClicked : ReminderInfoIntent
    data object SkipRemainingTodayClicked : ReminderInfoIntent
    data class EnabledChanged(
        val enabled: Boolean
    ) : ReminderInfoIntent
    data object BackClicked : ReminderInfoIntent
}
