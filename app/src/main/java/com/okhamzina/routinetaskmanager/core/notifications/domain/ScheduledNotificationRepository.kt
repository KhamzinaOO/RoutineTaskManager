package com.okhamzina.routinetaskmanager.core.notifications.domain

import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType

interface ScheduledNotificationRepository {

    suspend fun insert(notification: ScheduledNotification)

    suspend fun insertAll(notifications: List<ScheduledNotification>)

    suspend fun getAll(): List<ScheduledNotification>

    suspend fun getByTargetType(
        targetType: NotificationTargetType
    ): List<ScheduledNotification>

    suspend fun getByTargetTypeAndOccurrenceKind(
        targetType: NotificationTargetType,
        occurrenceKind: NotificationOccurrenceKind
    ): List<ScheduledNotification>

    suspend fun getByTargetTypeAndOccurrenceKeyPrefix(
        targetType: NotificationTargetType,
        occurrenceKeyPrefix: String
    ): List<ScheduledNotification>

    suspend fun getByTarget(
        targetType: NotificationTargetType,
        targetId: Long
    ): List<ScheduledNotification>

    suspend fun getByRequestCode(requestCode: Int): ScheduledNotification?

    suspend fun deleteByRequestCode(requestCode: Int)

    suspend fun deleteByTargetType(targetType: NotificationTargetType)

    suspend fun deleteByTargetTypeAndOccurrenceKind(
        targetType: NotificationTargetType,
        occurrenceKind: NotificationOccurrenceKind
    )

    suspend fun deleteByTarget(
        targetType: NotificationTargetType,
        targetId: Long
    )

    suspend fun deleteAll()
}
