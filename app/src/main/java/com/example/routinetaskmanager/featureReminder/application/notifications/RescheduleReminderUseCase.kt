package com.example.routinetaskmanager.featureReminder.application.notifications

import com.example.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.example.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.core.utills.toEpochMillis
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.example.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class RescheduleRemindersUseCase(
    private val reminderOccurrenceRepository: ReminderOccurrenceRepository,
    private val reminderRepository: ReminderRepository,
    private val scheduleCalculator: ReminderScheduleCalculator,
    private val alarmScheduler: AppAlarmScheduler,
    private val scheduledNotificationDao: ScheduledNotificationDao,
) {
    private val mutex: Mutex = Mutex()
    suspend operator fun invoke() {
        mutex.withLock {
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
                    endExclusive = now
                        .toLocalDate()
                        .plusDays(SCHEDULE_LOOK_AHEAD_DAYS + 1)
                        .atStartOfDay()
                )

                val statesByKey = reminderOccurrenceRepository.getByRange(
                    startMillis = range.start.toEpochMillis(),
                    endMillis = range.endExclusive.toEpochMillis()
                ).associateBy { it.occurrenceKey }

                val nextOccurrences = scheduleCalculator
                    .buildOccurrences(
                        reminders = reminders,
                        range = range
                    ).map { occurrence ->
                        val state = statesByKey[occurrence.occurrenceKey]

                        if(state == null){
                            occurrence
                        }else{
                            occurrence.copy(
                                status = state.status
                            )
                        }
                    }
                    .filter { occurrence ->
                        occurrence.scheduledAt.isAfter(now) && occurrence.status == ReminderOccurrenceStatus.PLANNED
                    }
                    .sortedBy { occurrence ->
                        occurrence.scheduledAt
                    }
                    .take(MAX_SCHEDULED_REMINDER_NOTIFICATIONS)

                val entities = nextOccurrences.mapNotNull { occurrence ->
                    val scheduledAtMillis = occurrence.scheduledAtMillis
                    val occurrenceKey = occurrence.occurrenceKey

                    //IS NOT UNIQUE!!!
                    val requestCode = occurrenceKey.hashCode()

                    val wasScheduled = alarmScheduler.schedule(
                        targetType = NotificationTargetType.REMINDER,
                        targetId = occurrence.reminderId,
                        scheduledAtMillis = scheduledAtMillis,
                        requestCode = requestCode,
                        precision = AlarmPrecision.EXACT
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
                        occurrenceKind = NotificationOccurrenceKind.REGULAR.name
                    )
                }

                scheduledNotificationDao.insertAll(entities)
            }
        }
    }

    private companion object {
        const val SCHEDULE_LOOK_AHEAD_DAYS = 7L
        const val MAX_SCHEDULED_REMINDER_NOTIFICATIONS = 30
    }
}
