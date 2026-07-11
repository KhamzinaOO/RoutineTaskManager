package com.okhamzina.routinetaskmanager.featureReminder.data.mapper

import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderImageEntity
import com.okhamzina.routinetaskmanager.featureReminder.data.local.ReminderWithImages
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderImage

fun ReminderWithImages.toDomain(): Reminder {
    return Reminder(
        id = reminder.id,
        name = reminder.name,
        instructionsText = reminder.instructionsText,
        repeatRule = ReminderRepeatRuleJsonMapper.fromJson(reminder.repeatRuleJson),
        notificationMode = NotificationMode.valueOf(reminder.notificationMode),
        images = images.map { it.toDomain() },
        createdAt = reminder.createdAt,
        updatedAt = reminder.updatedAt,
        isEnabled = reminder.isEnabled,
        notificationEnabled = reminder.notificationEnabled
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
        updatedAt = updatedAt,
        isEnabled = isEnabled,
        notificationEnabled = notificationEnabled
    )
}