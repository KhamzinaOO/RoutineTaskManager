package com.okhamzina.routinetaskmanager.featureReminder.domain.model

data class ReminderDraft(
    val name: String,
    val instructionsText: String?,
    val repeatRule: ReminderRepeatRule,
    val notificationMode: NotificationMode,
    val images: List<ReminderImageInput>
)