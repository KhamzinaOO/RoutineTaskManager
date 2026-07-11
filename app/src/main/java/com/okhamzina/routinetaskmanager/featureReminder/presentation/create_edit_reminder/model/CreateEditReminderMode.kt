package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model

sealed interface CreateEditReminderMode {
    data object Create : CreateEditReminderMode
    data class Edit(val reminderId: Long) : CreateEditReminderMode
}