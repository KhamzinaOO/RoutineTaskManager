package com.example.routinetaskmanager.core.notifications

import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.example.routinetaskmanager.featureReminder.domain.useCase.RescheduleRemindersUseCase
import java.time.Duration

class ReminderNotificationTriggerHandler(
    private val reminderRepository: ReminderRepository,
    private val scheduledNotificationDao: ScheduledNotificationDao,
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase
) : NotificationTriggerHandler {

    override suspend fun buildPayloadOrNull(
        targetId: Long,
        scheduledAtMillis: Long
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
            rescheduleRemindersUseCase()
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
        scheduledAtMillis: Long
    ) {
        rescheduleRemindersUseCase()
    }

    private companion object {
        val MAX_ALLOWED_DELAY: Duration = Duration.ofMinutes(15)
    }
}