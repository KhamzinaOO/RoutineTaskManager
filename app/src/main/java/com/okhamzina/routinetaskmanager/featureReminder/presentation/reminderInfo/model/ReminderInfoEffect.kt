package com.okhamzina.routinetaskmanager.featureReminder.presentation.reminderInfo.model

import com.okhamzina.routinetaskmanager.core.presentation.model.UiText

sealed interface ReminderInfoEffect {
    data class ShowMessage(val message: UiText): ReminderInfoEffect
    data object NavigateBack : ReminderInfoEffect
    data class NavigateToEditReminder(val id: Long) : ReminderInfoEffect
}
