package com.example.routinetaskmanager.featureReminder.presentation.create_reminder.viewModel

sealed interface CreateReminderEffect {
    data object OpenImagePicker : CreateReminderEffect
    data object NavigateBack : CreateReminderEffect

    data class ShowMessage(
        val message: String
    ) : CreateReminderEffect
}