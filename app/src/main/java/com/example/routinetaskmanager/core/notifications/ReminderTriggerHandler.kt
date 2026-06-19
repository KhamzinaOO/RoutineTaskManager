package com.example.routinetaskmanager.core.notifications

import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.domain.useCase.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.WorkSessionManager
import java.time.Duration

class ReminderNotificationTriggerHandler(
    private val reminderRepository: ReminderRepository,
    private val scheduledNotificationDao: ScheduledNotificationDao,
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
            scheduledNotificationDao.deleteByTarget(
                targetType = NotificationTargetType.REMINDER.name,
                targetId = targetId
            )
            return null
        }

        val delayMillis = System.currentTimeMillis() - scheduledAtMillis

        if (delayMillis > MAX_ALLOWED_DELAY.toMillis()) {
            rescheduleByOccurrenceKind(occurrenceKind)
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

    private companion object {
        val MAX_ALLOWED_DELAY: Duration = Duration.ofMinutes(15)
    }
}
