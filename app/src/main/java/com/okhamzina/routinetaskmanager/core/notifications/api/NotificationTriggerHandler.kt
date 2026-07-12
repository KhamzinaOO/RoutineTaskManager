package com.okhamzina.routinetaskmanager.core.notifications.api

interface NotificationTriggerHandler {
    suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind,
        occurrenceKey: String?
    ): NotificationPayload?

    suspend fun onNotificationShown(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    )
}
