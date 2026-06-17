package com.example.routinetaskmanager.featureReminder.presentation.reminder_main.model

import com.example.routinetaskmanager.core.presentation.model.UiText

sealed interface ReminderMainEffect {
    data object OpenDrawer : ReminderMainEffect
    data object FABClicked : ReminderMainEffect
    data class ShowMessage(val message: UiText) : ReminderMainEffect
}
