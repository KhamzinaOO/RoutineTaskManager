package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model

sealed interface ReminderMainEffect {
    data object OpenDrawer : ReminderMainEffect
    data object FABClicked : ReminderMainEffect
    data class ShowMessage(val message: String) : ReminderMainEffect
}
