package com.example.routinetaskmanager.core.notifications

class NotificationTriggerRouter(
    private val reminderHandler: ReminderNotificationTriggerHandler,
    private val taskHandler: TaskNotificationTriggerHandler
) {

    suspend fun buildPayloadOrNull(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long
    ): NotificationPayload? {
        return when (targetType) {
            NotificationTargetType.REMINDER -> {
                reminderHandler.buildPayloadOrNull(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis
                )
            }

            NotificationTargetType.TASK -> {
                taskHandler.buildPayloadOrNull(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis
                )
            }
        }
    }

    suspend fun onNotificationShown(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long
    ) {
        when (targetType) {
            NotificationTargetType.REMINDER -> {
                reminderHandler.onNotificationShown(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis
                )
            }

            NotificationTargetType.TASK -> {
                taskHandler.onNotificationShown(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis
                )
            }
        }
    }
}