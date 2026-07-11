package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.model

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode

data class ReminderMiniCardUi(
    val id: Long,
    val title: String,
    val repeatLabel: String,
    val detailsLabel: String?,
    val notificationMode: NotificationMode,
    val notificationEnabled: Boolean,
    val isEnabled: Boolean
)