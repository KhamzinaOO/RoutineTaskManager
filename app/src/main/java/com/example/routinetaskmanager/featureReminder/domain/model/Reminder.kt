package com.example.routinetaskmanager.featureReminder.domain.model

data class Reminder(
    val id: Long = 0,
    val name: String,
    val instructionsText: String?,
    val repeatRule: ReminderRepeatRule,
    val notificationMode: NotificationMode,
    val images: List<ReminderImage> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
)