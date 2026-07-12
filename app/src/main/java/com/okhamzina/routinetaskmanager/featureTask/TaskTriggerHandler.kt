package com.okhamzina.routinetaskmanager.featureTask

import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationPayload
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTriggerHandler

class TaskNotificationTriggerHandler : NotificationTriggerHandler {

    override suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind,
        occurrenceKey: String?
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
