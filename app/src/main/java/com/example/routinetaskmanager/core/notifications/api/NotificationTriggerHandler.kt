package com.example.routinetaskmanager.core.notifications.api

import com.example.routinetaskmanager.core.notifications.api.NotificationPayload

interface NotificationTriggerHandler {
    // TODO: adequate sealed result instead of null
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