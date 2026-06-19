package com.example.routinetaskmanager.featureReminder.domain.useCase

import com.example.routinetaskmanager.core.notifications.AlarmPrecision
import com.example.routinetaskmanager.core.notifications.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.NotificationOccurrenceKind
import com.example.routinetaskmanager.core.notifications.NotificationTargetType
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationEntity
import com.example.routinetaskmanager.core.notifications.toReminderChannelId
import com.example.routinetaskmanager.featureReminder.data.mapper.toRepeatTypeDomain
import com.example.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderSessionNotificationUseCase(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AppAlarmScheduler,
    private val scheduledNotificationDao: ScheduledNotificationDao
) {

    fun observeSessionOccurrences(
        startedAt: LocalDateTime
    ): Flow<List<ReminderOccurrence>> {
        return reminderRepository.observeReminders()
            .map { reminders ->
                val enabledReminders = reminders.filter { reminder ->
                    reminder.isEnabled && reminder.notificationEnabled
                }

                buildSessionOccurrences(
                    reminders = enabledReminders,
                    startedAt = startedAt
                )
                    .take(MAX_SESSION_NOTIFICATIONS)
                    .map { it.toReminderOccurrence() }
            }
    }

    suspend fun startSession(
        startedAt: LocalDateTime = LocalDateTime.now()
    ): SessionScheduleResult {
        return withContext(Dispatchers.IO) {
            cancelSessionNotifications()

            val reminders = reminderRepository.getAllRemindersSnapshot()
                .filter { reminder ->
                    reminder.isEnabled && reminder.notificationEnabled
                }

            val occurrences = buildSessionOccurrences(
                reminders = reminders,
                startedAt = startedAt
            ).take(MAX_SESSION_NOTIFICATIONS)

            val sessionReminderCount = reminders.count { reminder ->
                reminder.hasSessionReminderFor(startedAt)
            }

            val entities = occurrences.mapNotNull { occurrence ->
                val scheduledAtMillis = occurrence.scheduledAt
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val occurrenceKey = buildSessionOccurrenceKey(
                    reminderId = occurrence.reminder.id,
                    scheduledAtMillis = scheduledAtMillis,
                    sequence = occurrence.sequence
                )

                val requestCode = occurrenceKey.hashCode()

                val wasScheduled = alarmScheduler.schedule(
                    targetType = NotificationTargetType.REMINDER,
                    targetId = occurrence.reminder.id,
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
                    targetId = occurrence.reminder.id,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKey = occurrenceKey,
                    //channelId = occurrence.reminder.notificationMode.toReminderChannelId(),
                    occurrenceKind = NotificationOccurrenceKind.SESSION.name
                )
            }

            scheduledNotificationDao.insertAll(entities)
            SessionScheduleResult(
                sessionReminderCount = sessionReminderCount,
                scheduledNotificationCount = entities.size
            )
        }
    }

    suspend fun countSessionReminders(
        startedAt: LocalDateTime = LocalDateTime.now()
    ): Int {
        return withContext(Dispatchers.IO) {
            reminderRepository.getAllRemindersSnapshot()
                .count { reminder ->
                    reminder.isEnabled &&
                        reminder.notificationEnabled &&
                        reminder.hasSessionReminderFor(startedAt)
                }
        }
    }

    suspend fun endSession() {
        withContext(Dispatchers.IO) {
            cancelSessionNotifications()
        }
    }

    private suspend fun cancelSessionNotifications() {
        val sessionNotifications =
            scheduledNotificationDao.getByTargetTypeAndOccurrenceKind(
                targetType = NotificationTargetType.REMINDER.name,
                occurrenceKind = NotificationOccurrenceKind.SESSION.name
            )

        sessionNotifications.forEach { notification ->
            alarmScheduler.cancel(notification.requestCode)
        }

        scheduledNotificationDao.deleteByTargetTypeAndOccurrenceKind(
            targetType = NotificationTargetType.REMINDER.name,
            occurrenceKind = NotificationOccurrenceKind.SESSION.name
        )
    }

    private fun buildSessionOccurrences(
        reminders: List<Reminder>,
        startedAt: LocalDateTime
    ): List<SessionOccurrence> {
        val duringSession = reminders.flatMap { reminder ->
            buildDuringSessionOccurrences(
                reminder = reminder,
                startedAt = startedAt
            )
        }

        val afterAnother = buildAfterAnotherOccurrences(
            reminders = reminders,
            startedAt = startedAt
        )

        return (duringSession + afterAnother).sortedBy { it.scheduledAt }
    }

    private fun buildDuringSessionOccurrences(
        reminder: Reminder,
        startedAt: LocalDateTime
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

    private fun buildAfterAnotherOccurrences(
        reminders: List<Reminder>,
        startedAt: LocalDateTime
    ): List<SessionOccurrence> {
        var previousScheduledAt = startedAt

        return reminders
            .filter { reminder -> reminder.repeatRule is ReminderRepeatRule.AfterAnother }
            .sortedBy { reminder -> reminder.id }
            .mapIndexedNotNull { index, reminder ->
                val rule = reminder.repeatRule as ReminderRepeatRule.AfterAnother
                val waitDuration = rule.waitInterval.toDurationOrNull()
                    ?: return@mapIndexedNotNull null

                previousScheduledAt = previousScheduledAt.plus(waitDuration)

                SessionOccurrence(
                    reminder = reminder,
                    scheduledAt = previousScheduledAt,
                    sequence = index
                )
            }
    }

    private fun Reminder.hasSessionReminderFor(
        startedAt: LocalDateTime
    ): Boolean {
        return when (val rule = repeatRule) {
            is ReminderRepeatRule.AfterAnother -> {
                rule.waitInterval.toDurationOrNull() != null
            }

            is ReminderRepeatRule.DuringSessionPeriod -> {
                rule.schedule.valueForDay(startedAt.dayOfWeek)
                    ?.interval
                    ?.toDurationOrNull() != null
            }

            is ReminderRepeatRule.OnScheduleCertain,
            is ReminderRepeatRule.OnSchedulePeriod -> false
        }
    }

    private fun com.example.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat<IntervalRepeat>.valueForDay(
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
        return "$SESSION_OCCURRENCE_KEY_PREFIX$reminderId-$scheduledAtMillis-$sequence"
    }

    private data class SessionOccurrence(
        val reminder: Reminder,
        val scheduledAt: LocalDateTime,
        val sequence: Int
    )

    private fun SessionOccurrence.toReminderOccurrence(): ReminderOccurrence {
        return ReminderOccurrence(
            reminderId = reminder.id,
            reminderName = reminder.name,
            instructionsText = reminder.instructionsText,
            scheduledAt = scheduledAt,
            repeatType = reminder.repeatRule.toRepeatTypeDomain()
        )
    }

    data class SessionScheduleResult(
        val sessionReminderCount: Int,
        val scheduledNotificationCount: Int
    )

    private companion object {
        const val SESSION_OCCURRENCE_KEY_PREFIX = "REMINDER-SESSION-"
        const val MAX_SESSION_NOTIFICATIONS = 30
        const val MAX_OCCURRENCES_PER_SESSION_REMINDER = 12
        val SESSION_LOOK_AHEAD: Duration = Duration.ofHours(12)
    }
}
