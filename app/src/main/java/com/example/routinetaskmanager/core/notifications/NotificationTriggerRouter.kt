package com.example.routinetaskmanager.core.notifications

import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationPayload
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.featureReminder.application.notifications.ReminderNotificationTriggerHandler
import com.example.routinetaskmanager.featureTask.TaskNotificationTriggerHandler

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

    suspend fun onNotificationTriggered(
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
