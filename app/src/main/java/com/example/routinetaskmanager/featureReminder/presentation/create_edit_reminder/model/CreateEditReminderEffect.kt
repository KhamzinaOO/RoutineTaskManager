package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model

sealed interface CreateEditReminderEffect {
    data object OpenImagePicker : CreateEditReminderEffect
    data object NavigateBack : CreateEditReminderEffect
    data object RequestNotificationPermission : CreateEditReminderEffect

    data class ShowMessage(
        val message: String
    ) : CreateEditReminderEffect
}
