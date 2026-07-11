package com.okhamzina.routinetaskmanager.core.notifications.domain

import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType

data class ScheduledNotification(
    val id: Int = 0,
    val requestCode: Int,
    val targetType: NotificationTargetType,
    val targetId: Long,
    val scheduledAtMillis: Long,
    val occurrenceKey: String,
    val occurrenceKind: NotificationOccurrenceKind = NotificationOccurrenceKind.REGULAR,
    val createdAtMillis: Long = System.currentTimeMillis()
)
