package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model

import com.okhamzina.routinetaskmanager.core.presentation.model.UiText

sealed interface CreateEditReminderEffect {
    data object OpenImagePicker : CreateEditReminderEffect
    data object NavigateBack : CreateEditReminderEffect
    data object RequestNotificationPermission : CreateEditReminderEffect

    data class ShowMessage(
        val message: UiText
    ) : CreateEditReminderEffect
}
