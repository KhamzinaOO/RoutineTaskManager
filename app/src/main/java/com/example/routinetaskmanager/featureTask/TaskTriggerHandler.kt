package com.example.routinetaskmanager.featureTask

import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationPayload
import com.example.routinetaskmanager.core.notifications.api.NotificationTriggerHandler

class TaskNotificationTriggerHandler : NotificationTriggerHandler {

    override suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ): NotificationPayload? {
        return null
    }

    override suspend fun onNotificationShown(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ) {

    }
}
