package com.example.routinetaskmanager.core.notifications


interface NotificationTriggerHandler {

    suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ): NotificationPayload?

    suspend fun onNotificationShown(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    )
}
