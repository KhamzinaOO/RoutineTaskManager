package com.example.routinetaskmanager.featureReminder.application.notifications

import com.example.routinetaskmanager.core.notifications.api.NotificationPayload
import com.example.routinetaskmanager.core.notifications.api.NotificationTriggerHandler
import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.example.routinetaskmanager.core.notifications.toReminderChannelId
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager

class ReminderNotificationTriggerHandler(
    private val reminderRepository: ReminderRepository,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val workSessionManager: WorkSessionManager
) : NotificationTriggerHandler {

    override suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ): NotificationPayload? {
        val reminder = reminderRepository.getReminderById(targetId)
            ?: return null

        if (!reminder.isEnabled || !reminder.notificationEnabled) {
            scheduledNotificationRepository.deleteByTarget(
                targetType = NotificationTargetType.REMINDER,
                targetId = targetId
            )
            return null
        }

        return NotificationPayload(
            targetType = NotificationTargetType.REMINDER,
            targetId = reminder.id,
            title = reminder.name,
            text = reminder.instructionsText,
            scheduledAtMillis = scheduledAtMillis,
            channelId = reminder.notificationMode.toReminderChannelId()
        )
    }

    override suspend fun onNotificationShown(
        targetId: Long,
        scheduledAtMillis: Long,
        occurrenceKind: NotificationOccurrenceKind
    ) {
        rescheduleByOccurrenceKind(occurrenceKind)
    }

    private suspend fun rescheduleByOccurrenceKind(
        occurrenceKind: NotificationOccurrenceKind
    ) {
        when (occurrenceKind) {
            NotificationOccurrenceKind.REGULAR -> {
                rescheduleRemindersUseCase()
            }

            NotificationOccurrenceKind.SESSION -> {
                workSessionManager.rescheduleActiveSessionIfNeeded()
            }
        }
    }
}
