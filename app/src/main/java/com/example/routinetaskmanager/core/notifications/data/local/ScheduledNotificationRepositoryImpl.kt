package com.example.routinetaskmanager.core.notifications.data.local

import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.core.notifications.domain.ScheduledNotification
import com.example.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository

class ScheduledNotificationRepositoryImpl(
    private val scheduledNotificationDao: ScheduledNotificationDao
) : ScheduledNotificationRepository {

    override suspend fun insert(notification: ScheduledNotification) {
        scheduledNotificationDao.insert(notification.toEntity())
    }

    override suspend fun insertAll(notifications: List<ScheduledNotification>) {
        scheduledNotificationDao.insertAll(
            notifications.map { notification ->
                notification.toEntity()
            }
        )
    }

    override suspend fun getAll(): List<ScheduledNotification> {
        return scheduledNotificationDao.getAll()
            .map { entity -> entity.toDomain() }
    }

    override suspend fun getByTargetType(
        targetType: NotificationTargetType
    ): List<ScheduledNotification> {
        return scheduledNotificationDao.getByTargetType(targetType.name)
            .map { entity -> entity.toDomain() }
    }

    override suspend fun getByTargetTypeAndOccurrenceKind(
        targetType: NotificationTargetType,
        occurrenceKind: NotificationOccurrenceKind
    ): List<ScheduledNotification> {
        return scheduledNotificationDao.getByTargetTypeAndOccurrenceKind(
            targetType = targetType.name,
            occurrenceKind = occurrenceKind.name
        ).map { entity -> entity.toDomain() }
    }

    override suspend fun getByTargetTypeAndOccurrenceKeyPrefix(
        targetType: NotificationTargetType,
        occurrenceKeyPrefix: String
    ): List<ScheduledNotification> {
        return scheduledNotificationDao.getByTargetTypeAndOccurrenceKeyPrefix(
            targetType = targetType.name,
            occurrenceKeyPrefix = occurrenceKeyPrefix
        ).map { entity -> entity.toDomain() }
    }

    override suspend fun getByTarget(
        targetType: NotificationTargetType,
        targetId: Long
    ): List<ScheduledNotification> {
        return scheduledNotificationDao.getByTarget(
            targetType = targetType.name,
            targetId = targetId
        ).map { entity -> entity.toDomain() }
    }

    override suspend fun getByRequestCode(requestCode: Int): ScheduledNotification? {
        return scheduledNotificationDao.getByRequestCode(requestCode)
            ?.toDomain()
    }

    override suspend fun deleteByRequestCode(requestCode: Int) {
        scheduledNotificationDao.deleteByRequestCode(requestCode)
    }

    override suspend fun deleteByTargetType(targetType: NotificationTargetType) {
        scheduledNotificationDao.deleteByTargetType(targetType.name)
    }

    override suspend fun deleteByTargetTypeAndOccurrenceKind(
        targetType: NotificationTargetType,
        occurrenceKind: NotificationOccurrenceKind
    ) {
        scheduledNotificationDao.deleteByTargetTypeAndOccurrenceKind(
            targetType = targetType.name,
            occurrenceKind = occurrenceKind.name
        )
    }

    override suspend fun deleteByTarget(
        targetType: NotificationTargetType,
        targetId: Long
    ) {
        scheduledNotificationDao.deleteByTarget(
            targetType = targetType.name,
            targetId = targetId
        )
    }

    override suspend fun deleteAll() {
        scheduledNotificationDao.deleteAll()
    }

    private fun ScheduledNotification.toEntity(): ScheduledNotificationEntity {
        return ScheduledNotificationEntity(
            id = id,
            requestCode = requestCode,
            targetType = targetType.name,
            targetId = targetId,
            scheduledAtMillis = scheduledAtMillis,
            occurrenceKey = occurrenceKey,
            occurrenceKind = occurrenceKind.name,
            createdAtMillis = createdAtMillis
        )
    }

    private fun ScheduledNotificationEntity.toDomain(): ScheduledNotification {
        return ScheduledNotification(
            id = id,
            requestCode = requestCode,
            targetType = NotificationTargetType.valueOf(targetType),
            targetId = targetId,
            scheduledAtMillis = scheduledAtMillis,
            occurrenceKey = occurrenceKey,
            occurrenceKind = occurrenceKind.toOccurrenceKind(),
            createdAtMillis = createdAtMillis
        )
    }

    private fun String.toOccurrenceKind(): NotificationOccurrenceKind {
        return runCatching {
            NotificationOccurrenceKind.valueOf(this)
        }.getOrDefault(NotificationOccurrenceKind.REGULAR)
    }
}
