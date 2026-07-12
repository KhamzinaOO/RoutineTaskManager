package com.okhamzina.routinetaskmanager.core.notifications

import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationPayload
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationAction
import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.EmptyAppResult
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.ReminderNotificationTriggerHandler
import com.okhamzina.routinetaskmanager.featureTask.TaskNotificationTriggerHandler

class NotificationTriggerRouter(
    private val reminderHandler: ReminderNotificationTriggerHandler,
    private val taskHandler: TaskNotificationTriggerHandler
) {

    suspend fun buildPayloadOrNull(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind,
        occurrenceKey: String?
    ): NotificationPayload? {
        return when (targetType) {
            NotificationTargetType.REMINDER -> {
                reminderHandler.buildPayloadOrNull(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKind = occurrenceKind,
                    occurrenceKey = occurrenceKey
                )
            }

            NotificationTargetType.TASK -> {
                taskHandler.buildPayloadOrNull(
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKind = occurrenceKind,
                    occurrenceKey = occurrenceKey
                )
            }
        }
    }

    suspend fun onNotificationAction(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind,
        occurrenceKey: String,
        action: NotificationAction
    ): EmptyAppResult<AppError> {
        return when (targetType) {
            NotificationTargetType.REMINDER -> reminderHandler.onNotificationAction(
                targetId = targetId,
                scheduledAtMillis = scheduledAtMillis,
                occurrenceKind = occurrenceKind,
                occurrenceKey = occurrenceKey,
                action = action
            )

            NotificationTargetType.TASK -> AppResult.Success(Unit)
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
