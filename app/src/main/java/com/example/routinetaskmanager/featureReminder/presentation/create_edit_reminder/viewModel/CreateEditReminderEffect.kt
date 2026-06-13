package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.viewModel

sealed interface CreateEditReminderEffect {
    data object OpenImagePicker : CreateEditReminderEffect
    data object NavigateBack : CreateEditReminderEffect

    data class ShowMessage(
        val message: String
    ) : CreateEditReminderEffect
}