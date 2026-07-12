package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminder_main.model

import com.okhamzina.routinetaskmanager.core.presentation.model.UiText

sealed interface ReminderMainEffect {
    data object OpenDrawer : ReminderMainEffect
    data object NavigateToCreateReminder : ReminderMainEffect
    data object OpenSearch : ReminderMainEffect
    data object OpenCalendar : ReminderMainEffect
    data class ShowMessage(val message: UiText) : ReminderMainEffect
}
