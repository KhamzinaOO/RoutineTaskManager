package com.example.routinetaskmanager.featureReminder.application.notifications

import com.example.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.example.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.core.notifications.toReminderChannelId
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
    private val scheduledNotificationDao: ScheduledNotificationDao,
) {

    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            val oldReminderNotifications = scheduledNotificationDao.getByTargetTypeAndOccurrenceKind(
                targetType = NotificationTargetType.REMINDER.name,
                occurrenceKind = NotificationOccurrenceKind.REGULAR.name
            )

            oldReminderNotifications.forEach { entity ->
                alarmScheduler.cancel(entity.requestCode)
            }

            scheduledNotificationDao.deleteByTargetTypeAndOccurrenceKind(
                targetType = NotificationTargetType.REMINDER.name,
                occurrenceKind = NotificationOccurrenceKind.REGULAR.name
            )

            val reminders = reminderRepository.getAllRemindersSnapshot()
                .filter { reminder ->
                    reminder.isEnabled && reminder.notificationEnabled
                }

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

            val entities = nextOccurrences.mapNotNull { occurrence ->
                val scheduledAtMillis = occurrence.scheduledAt
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val occurrenceKey = buildOccurrenceKey(
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminderId,
                    scheduledAtMillis = scheduledAtMillis
                )

                //IS NOT UNIQUE!!!
                val requestCode = occurrenceKey.hashCode()

                val reminder = reminders.first {
                    it.id == occurrence.reminderId
                }

                val channelId = reminder.notificationMode.toReminderChannelId()

                val wasScheduled = alarmScheduler.schedule(
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminderId,
                    scheduledAtMillis = scheduledAtMillis,
                    requestCode = requestCode,
                    precision = AlarmPrecision.INEXACT
                )

                if (!wasScheduled) {
                    return@mapNotNull null
                }

                ScheduledNotificationEntity(
                    requestCode = requestCode,
                    targetType = NotificationTargetType.REMINDER.name,
                    targetId = occurrence.reminderId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKey = occurrenceKey,
                    //channelId = channelId,
                    occurrenceKind = NotificationOccurrenceKind.REGULAR.name
                )
            }

            scheduledNotificationDao.insertAll(entities)
        }
    }

    private fun buildOccurrenceKey(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long
    ): String {
        return "${targetType.name}-$targetId-$scheduledAtMillis"
    }

    private companion object {
        const val SCHEDULE_LOOK_AHEAD_DAYS = 7L
        const val MAX_SCHEDULED_REMINDER_NOTIFICATIONS = 30
    }
}
