package com.example.routinetaskmanager.core.notifications

class TaskNotificationTriggerHandler : NotificationTriggerHandler {

    override suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long
    ): NotificationPayload? {
        return null
    }

    override suspend fun onNotificationShown(
        targetId: Long,
        scheduledAtMillis: Long
    ) {

    }
}
