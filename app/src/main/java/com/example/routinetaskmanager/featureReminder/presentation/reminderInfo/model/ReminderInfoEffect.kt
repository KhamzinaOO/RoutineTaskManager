package com.example.routinetaskmanager.featureReminder.presentation.reminderInfo.model

import com.example.routinetaskmanager.core.presentation.model.UiText

sealed interface ReminderInfoEffect {
    data class ShowMessage(val message: UiText): ReminderInfoEffect
    data object NavigateBack : ReminderInfoEffect
    data class EditReminder(val id: Long) : ReminderInfoEffect
}