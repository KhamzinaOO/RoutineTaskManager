package com.example.routinetaskmanager.core.notifications

class NotificationTriggerRouter(
    private val reminderHandler: ReminderNotificationTriggerHandler,
    private val taskHandler: TaskNotificationTriggerHandler
) {

    suspend fun buildPayloadOrNull(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ): NotificationPayload? {
        return when (targetType) {
            NotificationTargetType.REMINDER -> {
                reminderHandler.buildPayloadOrNull(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKind = occurrenceKind
                )
            }

            NotificationTargetType.TASK -> {
                taskHandler.buildPayloadOrNull(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKind = occurrenceKind
                )
            }
        }
    }

    suspend fun onNotificationShown(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ) {
        when (targetType) {
            NotificationTargetType.REMINDER -> {
                reminderHandler.onNotificationShown(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKind = occurrenceKind
                )
            }

            NotificationTargetType.TASK -> {
                taskHandler.onNotificationShown(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKind = occurrenceKind
                )
            }
        }
    }
}
