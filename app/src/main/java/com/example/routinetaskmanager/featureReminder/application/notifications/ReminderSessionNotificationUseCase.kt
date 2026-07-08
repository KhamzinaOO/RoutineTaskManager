package com.example.routinetaskmanager.featureReminder.application.notifications

import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.core.notifications.NotificationRequestCodeGenerator
import com.example.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.example.routinetaskmanager.core.notifications.api.AppAlarmScheduleResult
import com.example.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.example.routinetaskmanager.core.notifications.domain.ScheduledNotification
import com.example.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.example.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceKeyFactory
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.type
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderSessionNotificationUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AppAlarmScheduler,
    private val scheduledNotificationRepository: ScheduledNotificationRepository
) {

    fun observeSessionOccurrences(
        startedAt: LocalDateTime
    ): Flow<List<ReminderOccurrence>> {
        return reminderRepository.observeReminders()
            .map { reminders ->
                val now = LocalDateTime.now()
                val enabledReminders = reminders.filter { reminder ->
                    reminder.isEnabled && reminder.notificationEnabled
                }

                buildSessionOccurrences(
                    reminders = enabledReminders,
                    startedAt = startedAt,
                    from = now
                )
                    .take(MAX_SESSION_NOTIFICATIONS)
                    .map { it.toReminderOccurrence() }
            }
    }

    fun observeSessionOccurrenceOfReminderById(
        reminderId: Long,
        startedAt: LocalDateTime
    ): Flow<List<ReminderOccurrence>?> {
        return reminderRepository.observeReminderById(reminderId)
            .map { reminder ->
                reminder?.takeIf { it.isEnabled }
                    ?.let {
                        val now = LocalDateTime.now()
                        buildSessionOccurrences(
                            reminders = listOf(reminder),
                            startedAt = startedAt,
                            from = now
                        )
                            .take(MAX_SESSION_NOTIFICATIONS)
                            .map { it.toReminderOccurrence() }
                    }
            }
    }


    suspend fun rescheduleSessionNotifications(
        startedAt: LocalDateTime = LocalDateTime.now(),
        from: LocalDateTime = LocalDateTime.now()
    ): SessionScheduleResult {
        return withContext(dispatcherProvider.io) {
            cancelSessionNotifications()

            val usedRequestCodes = scheduledNotificationRepository
                .getAll()
                .map { notification -> notification.requestCode }
                .toMutableSet()

            val reminders = reminderRepository.getAllRemindersSnapshot()
                .filter { reminder ->
                    reminder.isEnabled && reminder.notificationEnabled
                }

            val occurrences = buildSessionOccurrences(
                reminders = reminders,
                startedAt = startedAt,
                from = from
            )
                .filter { it.scheduledAt.isAfter(from) }
                .take(MAX_SESSION_NOTIFICATIONS)

            val sessionReminderCount = reminders.count { reminder ->
                reminder.hasSessionReminderFor(startedAt)
            }

            val notifications = occurrences.mapNotNull { occurrence ->
                val scheduledAtMillis = occurrence.scheduledAt
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val occurrenceKey = buildSessionOccurrenceKey(
                    reminderId = occurrence.reminder.id,
                    scheduledAtMillis = scheduledAtMillis,
                    sequence = occurrence.sequence
                )

                val requestCode = NotificationRequestCodeGenerator.next(
                    key = occurrenceKey,
                    usedCodes = usedRequestCodes
                )

                val scheduleResult = alarmScheduler.schedule(
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminder.id,
                    scheduledAtMillis = scheduledAtMillis,
                    requestCode = requestCode,
                    precision = AlarmPrecision.INEXACT,
                    occurrenceKind = NotificationOccurrenceKind.SESSION
                )

                if (scheduleResult != AppAlarmScheduleResult.Scheduled) {
                    return@mapNotNull null
                }

                ScheduledNotification(
                    requestCode = requestCode,
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminder.id,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKey = occurrenceKey,
                    occurrenceKind = NotificationOccurrenceKind.SESSION
                )
            }

            scheduledNotificationRepository.insertAll(notifications)
            SessionScheduleResult(
                sessionReminderCount = sessionReminderCount,
                scheduledNotificationCount = notifications.size
            )
        }
    }

    suspend fun countSessionReminders(
        startedAt: LocalDateTime = LocalDateTime.now()
    ): Int {
        return withContext(dispatcherProvider.io) {
            reminderRepository.getAllRemindersSnapshot()
                .count { reminder ->
                    reminder.isEnabled &&
                            reminder.notificationEnabled &&
                            reminder.hasSessionReminderFor(startedAt)
                }
        }
    }

    suspend fun endSession() {
        withContext(dispatcherProvider.io) {
            cancelSessionNotifications()
        }
    }

    private suspend fun cancelSessionNotifications() {
        val sessionNotifications =
            scheduledNotificationRepository.getByTargetTypeAndOccurrenceKind(
                targetType = NotificationTargetType.REMINDER,
                occurrenceKind = NotificationOccurrenceKind.SESSION
            )

        sessionNotifications.forEach { notification ->
            alarmScheduler.cancel(notification.requestCode)
        }

        scheduledNotificationRepository.deleteByTargetTypeAndOccurrenceKind(
            targetType = NotificationTargetType.REMINDER,
            occurrenceKind = NotificationOccurrenceKind.SESSION
        )
    }

    private fun buildSessionOccurrences(
        reminders: List<Reminder>,
        startedAt: LocalDateTime,
        from : LocalDateTime
    ): List<SessionOccurrence> {
        val duringSession = reminders.flatMap { reminder ->
            buildDuringSessionOccurrences(
                reminder = reminder,
                startedAt = startedAt,
                from = from
            )
        }

        return (duringSession).sortedBy { it.scheduledAt }
    }

    private fun buildDuringSessionOccurrences(
        reminder: Reminder,
        startedAt: LocalDateTime,
        from: LocalDateTime
    ): List<SessionOccurrence> {
        val rule = reminder.repeatRule as? ReminderRepeatRule.DuringSessionPeriod
            ?: return emptyList()

        val dayRepeat = rule.schedule.valueForDay(startedAt.dayOfWeek)
            ?: return emptyList()

        val step = dayRepeat.interval.toDurationOrNull()
            ?: return emptyList()

        val result = mutableListOf<SessionOccurrence>()
        val sessionEnd = startedAt.plus(SESSION_LOOK_AHEAD)
        var current = startedAt.plus(step)
        var sequence = 0

        while (!current.isAfter(from)){
            current = current.plus(step)
            sequence += 1
        }

        while (!current.isAfter(sessionEnd) && result.size < MAX_OCCURRENCES_PER_SESSION_REMINDER) {
            result.add(
                SessionOccurrence(
                    reminder = reminder,
                    scheduledAt = current,
                    sequence = sequence
                )
            )
            current = current.plus(step)
            sequence += 1
        }

        return result
    }
    private fun Reminder.hasSessionReminderFor(
        startedAt: LocalDateTime
    ): Boolean {
        return when (val rule = repeatRule) {
            is ReminderRepeatRule.DuringSessionPeriod -> {
                rule.schedule.valueForDay(startedAt.dayOfWeek)
                    ?.interval
                    ?.toDurationOrNull() != null
            }

            is ReminderRepeatRule.OnScheduleCertain,
            is ReminderRepeatRule.OnSchedulePeriod -> false
        }
    }

    private fun WeeklyRepeat<IntervalRepeat>.valueForDay(
        dayOfWeek: DayOfWeek
    ): IntervalRepeat? {
        return when (mode) {
            RepeatScheduleMode.DEFAULT -> {
                if (dayOfWeek in selectedDays) defaultValue else null
            }

            RepeatScheduleMode.ADVANCED -> {
                advancedEntries
                    .firstOrNull { entry -> entry.day == dayOfWeek && entry.enabled }
                    ?.value
            }
        }
    }

    private fun RepeatInterval.toDurationOrNull(): Duration? {
        if (value <= 0) return null

        return when (unit) {
            RepeatUnit.MINUTES -> Duration.ofMinutes(value.toLong())
            RepeatUnit.HOURS -> Duration.ofHours(value.toLong())
            RepeatUnit.DAYS -> Duration.ofDays(value.toLong())
        }
    }

    private fun buildSessionOccurrenceKey(
        reminderId: Long,
        scheduledAtMillis: Long,
        sequence: Int
    ): String {
        return ReminderOccurrenceKeyFactory.session(
            reminderId = reminderId,
            scheduledAtMillis = scheduledAtMillis,
            sequence = sequence
        )
    }

    private data class SessionOccurrence(
        val reminder: Reminder,
        val scheduledAt: LocalDateTime,
        val sequence: Int
    )

    private fun SessionOccurrence.toReminderOccurrence(): ReminderOccurrence {
        val scheduledAtMillis = scheduledAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return ReminderOccurrence(
            reminderId = reminder.id,
            reminderName = reminder.name,
            instructionsText = reminder.instructionsText,
            scheduledAt = scheduledAt,
            repeatType = reminder.repeatRule.type,
            occurrenceKey = buildSessionOccurrenceKey(
                reminderId = reminder.id,
                scheduledAtMillis = scheduledAtMillis,
                sequence = sequence
            ),
            scheduledAtMillis = scheduledAtMillis,
            occurrenceKind = NotificationOccurrenceKind.SESSION
        )
    }

    data class SessionScheduleResult(
        val sessionReminderCount: Int,
        val scheduledNotificationCount: Int
    )

    private companion object {
        const val MAX_SESSION_NOTIFICATIONS = 30
        const val MAX_OCCURRENCES_PER_SESSION_REMINDER = 12
        val SESSION_LOOK_AHEAD: Duration = Duration.ofHours(12)
    }
}
