package com.example.routinetaskmanager.featureReminder.data.mapper

import com.example.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.example.routinetaskmanager.featureReminder.data.local.ReminderWithImages
import com.example.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderImage

fun ReminderWithImages.toDomain(): Reminder {
    return Reminder(
        id = reminder.id,
        name = reminder.name,
        instructionsText = reminder.instructionsText,
        repeatRule = ReminderRepeatRuleJsonMapper.fromJson(reminder.repeatRuleJson),
        notificationMode = NotificationMode.valueOf(reminder.notificationMode),
        images = images.map { it.toDomain() },
        createdAt = reminder.createdAt,
        updatedAt = reminder.updatedAt
    )
}

fun ReminderImageEntity.toDomain(): ReminderImage {
    return ReminderImage(
        id = id,
        reminderId = reminderId,
        imagePath = imagePath,
        sortOrder = sortOrder
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        name = name,
        instructionsText = instructionsText,
        repeatType = repeatRule.toRepeatType(),
        repeatRuleJson = ReminderRepeatRuleJsonMapper.toJson(repeatRule),
        notificationMode = notificationMode.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}