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
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceState
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ReminderScheduleCalculator
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule.ScheduleRange
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import java.time.LocalDateTime
import java.util.PriorityQueue

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
                val oldNotificationsByKey = oldReminderNotifications.associateBy { notification ->
                    notification.occurrenceKey
                }
                val oldNotificationsByRequestCode = oldReminderNotifications.associateBy { notification ->
                    notification.requestCode
                }

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

                val nextOccurrences = withContext(dispatcherProvider.default) {
                    findNextOccurrences(
                        reminders = reminders,
                        range = range,
                        statesByKey = statesByKey,
                        now = now,
                        limit = MAX_SCHEDULED_REMINDER_NOTIFICATIONS
                    )
                }

                val scheduledNotifications = mutableListOf<ScheduledNotification>()
                var databaseCommitted = false

                try {
                    for (occurrence in nextOccurrences) {
                        val existingNotification = oldNotificationsByKey[occurrence.occurrenceKey]
                        val requestCode = existingNotification?.requestCode
                            ?: NotificationRequestCodeGenerator.next(
                                key = occurrence.occurrenceKey,
                                usedCodes = usedRequestCodes
                            )

                        val notification = ScheduledNotification(
                            id = existingNotification?.id ?: 0,
                            requestCode = requestCode,
                            targetType = NotificationTargetType.REMINDER,
                            targetId = occurrence.reminderId,
                            scheduledAtMillis = occurrence.scheduledAtMillis,
                            occurrenceKey = occurrence.occurrenceKey,
                            occurrenceKind = NotificationOccurrenceKind.REGULAR,
                            createdAtMillis = existingNotification?.createdAtMillis
                                ?: System.currentTimeMillis()
                        )

                        when (val scheduleResult = schedule(notification)) {
                            AppAlarmScheduleResult.Scheduled -> {
                                scheduledNotifications += notification
                            }

                            AppAlarmScheduleResult.TimeInPast -> Unit

                            else -> {
                                rollbackScheduledNotifications(
                                    appliedNotifications = scheduledNotifications,
                                    oldNotificationsByRequestCode = oldNotificationsByRequestCode
                                )
                                return@withContext AppResult.Error(
                                    scheduleResult.toAppErrorOrNull()
                                        ?: AppError.AlarmSchedulingFailed()
                                )
                            }
                        }
                    }

                    val scheduledRequestCodes = scheduledNotifications
                        .mapTo(mutableSetOf()) { notification -> notification.requestCode }
                    val obsoleteNotifications = oldReminderNotifications.filter { notification ->
                        notification.requestCode !in scheduledRequestCodes
                    }

                    withContext(NonCancellable) {
                        scheduledNotificationRepository.replaceByTargetTypeAndOccurrenceKind(
                            targetType = NotificationTargetType.REMINDER,
                            occurrenceKind = NotificationOccurrenceKind.REGULAR,
                            notifications = scheduledNotifications
                        )
                        databaseCommitted = true

                        obsoleteNotifications.forEach { notification ->
                            alarmScheduler.cancel(notification.requestCode)
                        }
                    }

                    AppResult.Success(Unit)
                } catch (cancellation: CancellationException) {
                    if (!databaseCommitted) {
                        rollbackScheduledNotifications(
                            appliedNotifications = scheduledNotifications,
                            oldNotificationsByRequestCode = oldNotificationsByRequestCode
                        )
                    }
                    throw cancellation
                } catch (throwable: Throwable) {
                    if (!databaseCommitted) {
                        rollbackScheduledNotifications(
                            appliedNotifications = scheduledNotifications,
                            oldNotificationsByRequestCode = oldNotificationsByRequestCode
                        )
                    }
                    throw throwable
                }
            }
        }
    }

    private fun schedule(
        notification: ScheduledNotification
    ): AppAlarmScheduleResult {
        return alarmScheduler.schedule(
            targetType = notification.targetType,
            targetId = notification.targetId,
            scheduledAtMillis = notification.scheduledAtMillis,
            requestCode = notification.requestCode,
            precision = AlarmPrecision.EXACT,
            occurrenceKind = notification.occurrenceKind
        )
    }

    private suspend fun rollbackScheduledNotifications(
        appliedNotifications: List<ScheduledNotification>,
        oldNotificationsByRequestCode: Map<Int, ScheduledNotification>
    ) {
        withContext(dispatcherProvider.io + NonCancellable) {
            appliedNotifications.asReversed().forEach { notification ->
                val previousNotification = oldNotificationsByRequestCode[notification.requestCode]

                if (previousNotification == null) {
                    alarmScheduler.cancel(notification.requestCode)
                } else {
                    schedule(previousNotification)
                }
            }
        }
    }

    private fun findNextOccurrences(
        reminders: List<Reminder>,
        range: ScheduleRange,
        statesByKey: Map<String, ReminderOccurrenceState>,
        now: LocalDateTime,
        limit: Int
    ): List<ReminderOccurrence> {
        require(limit > 0) { "Occurrence limit must be positive" }

        val nearestOccurrences = PriorityQueue<ReminderOccurrence>(
            limit,
            compareByDescending { occurrence -> occurrence.scheduledAtMillis }
        )

        scheduleCalculator.generateOccurrences(reminders, range).forEach { occurrence ->
            val status = statesByKey[occurrence.occurrenceKey]?.status
                ?: occurrence.status

            if (
                status != ReminderOccurrenceStatus.PLANNED ||
                !occurrence.scheduledAt.isAfter(now)
            ) {
                return@forEach
            }

            val occurrenceWithState = if (status == occurrence.status) {
                occurrence
            } else {
                occurrence.copy(status = status)
            }

            if (nearestOccurrences.size < limit) {
                nearestOccurrences += occurrenceWithState
            } else {
                val latestKeptOccurrence = nearestOccurrences.peek()
                if (
                    latestKeptOccurrence != null &&
                    occurrenceWithState.scheduledAtMillis < latestKeptOccurrence.scheduledAtMillis
                ) {
                    nearestOccurrences.poll()
                    nearestOccurrences += occurrenceWithState
                }
            }
        }

        return nearestOccurrences.sortedBy { occurrence -> occurrence.scheduledAtMillis }
    }

    private companion object {
        const val SCHEDULE_LOOK_AHEAD_DAYS = 7L
        const val MAX_SCHEDULED_REMINDER_NOTIFICATIONS = 30
    }
}
