package com.okhamzina.routinetaskmanager.featureReminder.domain.model

data class ReminderImage(
    val id: Long = 0,
    val reminderId: Long,
    val imagePath: String,
    val sortOrder: Int
)