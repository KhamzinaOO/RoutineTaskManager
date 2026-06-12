package com.example.routinetaskmanager.featureReminder.domain.useCase

import com.example.routinetaskmanager.core.notifications.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.NotificationPayload
import com.example.routinetaskmanager.core.notifications.NotificationTargetType
import com.example.routinetaskmanager.core.notifications.ScheduledNotificationDAO
import com.example.routinetaskmanager.core.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId

class RescheduleRemindersUseCase(
    private val reminderRepository: ReminderRepository,
    private val scheduleCalculator: ReminderScheduleCalculator,
    private val alarmScheduler: AppAlarmScheduler,
    private val scheduledNotificationDao: ScheduledNotificationDAO
) {

    suspend operator fun invoke() {
        withContext(Dispatchers.IO){
            val oldReminderNotifications = scheduledNotificationDao.getByTargetType(
                targetType = NotificationTargetType.REMINDER
            )

            oldReminderNotifications.forEach { entity ->
                alarmScheduler.cancel(entity.requestCode)
            }

            scheduledNotificationDao.deleteByTargetType(
                targetType = NotificationTargetType.REMINDER.name
            )

            val reminders = reminderRepository.getAllRemindersSnapshot()

            val now = LocalDateTime.now()

            val range = ScheduleRange(
                start = now,
                endExclusive = now.plusDays(SCHEDULE_LOOK_AHEAD_DAYS)
            )

            val nextOccurrences = scheduleCalculator
                .buildOccurrences(
                    reminders = reminders,
                    range = range
                )
                .filter { occurrence ->
                    occurrence.scheduledAt.isAfter(now)
                }
                .sortedBy { occurrence ->
                    occurrence.scheduledAt
                }
                .take(MAX_SCHEDULED_REMINDER_NOTIFICATIONS)

            val scheduledEntities = nextOccurrences.map { occurrence ->
                val scheduledAtMillis = occurrence.scheduledAt
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val requestCode = buildRequestCode(
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminderId,
                    scheduledAtMillis = scheduledAtMillis
                )

                val payload = NotificationPayload(
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminderId,
                    title = occurrence.reminderName,
                    text = occurrence.instructionsText,
                    scheduledAtMillis = scheduledAtMillis
                )

                alarmScheduler.schedule(
                    payload = payload,
                    requestCode = requestCode
                )

                ScheduledNotificationEntity(
                    requestCode = requestCode,
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminderId,
                    scheduledAtMillis = scheduledAtMillis,
                    title = occurrence.reminderName,
                    text = occurrence.instructionsText
                )
            }

            scheduledNotificationDao.insertAll(scheduledEntities)
        }
    }

    private fun buildRequestCode(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long
    ): Int {
        return "${targetType.name}-$targetId-$scheduledAtMillis".hashCode()
    }

    private companion object {
        const val SCHEDULE_LOOK_AHEAD_DAYS = 7L
        const val MAX_SCHEDULED_REMINDER_NOTIFICATIONS = 30
    }
}