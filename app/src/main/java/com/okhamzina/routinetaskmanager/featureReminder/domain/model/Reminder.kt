package com.okhamzina.routinetaskmanager.featureReminder.domain.model

data class Reminder(
    val id: Long = 0,
    val name: String,
    val instructionsText: String?,
    val repeatRule: ReminderRepeatRule,
    val notificationMode: NotificationMode,
    val images: List<ReminderImage> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val isEnabled: Boolean = true,
    val notificationEnabled: Boolean = true,
)