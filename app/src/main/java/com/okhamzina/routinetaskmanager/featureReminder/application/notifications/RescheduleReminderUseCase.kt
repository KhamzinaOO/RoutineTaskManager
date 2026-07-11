package com.okhamzina.routinetaskmanager.featureReminder.application.notifications

import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.EmptyAppResult
import com.okhamzina.routinetaskmanager.core.notifications.NotificationRequestCodeGenerator
import com.okhamzina.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduleResult
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.okhamzina.routinetaskmanager.core.notifications.api.toAppErrorOrNull
import com.okhamzina.routinetaskmanager.core.utills.toEpochMillis
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotification
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class RescheduleRemindersUseCase(
    private val reminderOccurrenceRepository: ReminderOccurrenceRepository,
    private val reminderRepository: ReminderRepository,
    private val scheduleCalculator: ReminderScheduleCalculator,
    private val alarmScheduler: AppAlarmScheduler,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    private val mutex: Mutex = Mutex()
    suspend operator fun invoke(): EmptyAppResult<AppError> {
        return mutex.withLock {
            withContext(dispatcherProvider.io) {
                val oldReminderNotifications = scheduledNotificationRepository.getByTargetTypeAndOccurrenceKind(
                    targetType = NotificationTargetType.REMINDER,
                    occurrenceKind = NotificationOccurrenceKind.REGULAR
                )

                oldReminderNotifications.forEach { entity ->
                    alarmScheduler.cancel(entity.requestCode)
                }

                scheduledNotificationRepository.deleteByTargetTypeAndOccurrenceKind(
                    targetType = NotificationTargetType.REMINDER,
                    occurrenceKind = NotificationOccurrenceKind.REGULAR
                )

                val usedRequestCodes = scheduledNotificationRepository
                    .getAll()
                    .map { notification -> notification.requestCode }
                    .toMutableSet()

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

                val notifications = mutableListOf<ScheduledNotification>()

                for (occurrence in nextOccurrences) {
                    val scheduledAtMillis = occurrence.scheduledAtMillis
                    val occurrenceKey = occurrence.occurrenceKey

                    val requestCode = NotificationRequestCodeGenerator.next(
                        key = occurrenceKey,
                        usedCodes = usedRequestCodes
                    )

                    val scheduleResult = alarmScheduler.schedule(
                        targetType = NotificationTargetType.REMINDER,
                        targetId = occurrence.reminderId,
                        scheduledAtMillis = scheduledAtMillis,
                        requestCode = requestCode,
                        precision = AlarmPrecision.EXACT,
                        occurrenceKind = NotificationOccurrenceKind.REGULAR
                    )

                    when (scheduleResult) {
                        AppAlarmScheduleResult.Scheduled -> {
                            notifications += ScheduledNotification(
                                requestCode = requestCode,
                                targetType = NotificationTargetType.REMINDER,
                                targetId = occurrence.reminderId,
                                scheduledAtMillis = scheduledAtMillis,
                                occurrenceKey = occurrenceKey,
                                occurrenceKind = NotificationOccurrenceKind.REGULAR
                            )
                        }

                        AppAlarmScheduleResult.TimeInPast -> Unit

                        else -> {
                            notifications.forEach { notification ->
                                alarmScheduler.cancel(notification.requestCode)
                            }
                            return@withContext AppResult.Error(
                                scheduleResult.toAppErrorOrNull()
                                    ?: AppError.AlarmSchedulingFailed()
                            )
                        }
                    }
                }

                scheduledNotificationRepository.insertAll(notifications)
                AppResult.Success(Unit)
            }
        }
    }

    private companion object {
        const val SCHEDULE_LOOK_AHEAD_DAYS = 7L
        const val MAX_SCHEDULED_REMINDER_NOTIFICATIONS = 30
    }
}
