package com.okhamzina.routinetaskmanager.featureReminder.application.notifications

import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.notifications.NotificationRequestCodeGenerator
import com.okhamzina.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduleResult
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.okhamzina.routinetaskmanager.core.notifications.api.toAppErrorOrNull
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotification
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceKeyFactory
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.type
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.repository.ReminderOccurrenceRepository
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceStatus
import com.okhamzina.routinetaskmanager.core.utills.toEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderSessionNotificationUseCase(
    private val dispatcherProvider: DispatcherProvider,
    private val reminderRepository: ReminderRepository,
    private val reminderOccurrenceRepository: ReminderOccurrenceRepository,
    private val alarmScheduler: AppAlarmScheduler,
    private val scheduledNotificationRepository: ScheduledNotificationRepository
) {

    private val rescheduleMutex = Mutex()

    fun observeSessionOccurrences(
        startedAt: LocalDateTime
    ): Flow<List<ReminderOccurrence>> {
        return combine(
            reminderRepository.observeReminders(),
            observeSessionOccurrenceStates(startedAt)
        ) { reminders, states ->
                val statesByKey = states.associateBy { state -> state.occurrenceKey }
                val enabledReminders = reminders.filter { reminder ->
                    reminder.isEnabled && reminder.notificationEnabled
                }

                buildSessionOccurrences(
                    reminders = enabledReminders,
                    startedAt = startedAt,
                    from = startedAt
                )
                    .take(MAX_SESSION_NOTIFICATIONS)
                    .map { occurrence ->
                        occurrence.toReminderOccurrence().let { domainOccurrence ->
                            domainOccurrence.copy(
                                status = statesByKey[domainOccurrence.occurrenceKey]?.status
                                    ?: ReminderOccurrenceStatus.PLANNED
                            )
                        }
                    }
            }
    }

    fun observeSessionOccurrenceOfReminderById(
        reminderId: Long,
        startedAt: LocalDateTime
    ): Flow<List<ReminderOccurrence>?> {
        return combine(
            reminderRepository.observeReminderById(reminderId),
            observeSessionOccurrenceStates(startedAt)
        ) { reminder, states ->
                val statesByKey = states.associateBy { state -> state.occurrenceKey }
                reminder?.takeIf { it.isEnabled }
                    ?.let {
                        buildSessionOccurrences(
                            reminders = listOf(reminder),
                            startedAt = startedAt,
                            from = startedAt
                        )
                            .take(MAX_SESSION_NOTIFICATIONS)
                            .map { occurrence ->
                                occurrence.toReminderOccurrence().let { domainOccurrence ->
                                    domainOccurrence.copy(
                                        status = statesByKey[domainOccurrence.occurrenceKey]?.status
                                            ?: ReminderOccurrenceStatus.PLANNED
                                    )
                                }
                            }
                    }
            }
    }


    suspend fun rescheduleSessionNotifications(
        startedAt: LocalDateTime = LocalDateTime.now(),
        from: LocalDateTime = LocalDateTime.now()
    ): AppResult<SessionScheduleResult, AppError> {
        return rescheduleMutex.withLock {
            withContext(dispatcherProvider.io) {
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
                .map { occurrence -> occurrence.toReminderOccurrence() }
                .let { builtOccurrences ->
                    val statesByKey = reminderOccurrenceRepository.getByRange(
                        startMillis = startedAt.toEpochMillis(),
                        endMillis = startedAt.plus(SESSION_LOOK_AHEAD).toEpochMillis()
                    ).associateBy { state -> state.occurrenceKey }

                    builtOccurrences.map { occurrence ->
                        occurrence.copy(
                            status = statesByKey[occurrence.occurrenceKey]?.status
                                ?: ReminderOccurrenceStatus.PLANNED
                        )
                    }
                }
                .filter { occurrence ->
                    occurrence.scheduledAt.isAfter(from) &&
                            occurrence.status == ReminderOccurrenceStatus.PLANNED
                }
                .take(MAX_SESSION_NOTIFICATIONS)

            val sessionReminderCount = reminders.count { reminder ->
                reminder.hasSessionReminderFor(startedAt)
            }

            val notifications = mutableListOf<ScheduledNotification>()

            for (occurrence in occurrences) {
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
                    occurrenceKind = NotificationOccurrenceKind.SESSION
                )

                when (scheduleResult) {
                    AppAlarmScheduleResult.Scheduled -> {
                        notifications += ScheduledNotification(
                            requestCode = requestCode,
                            targetType = NotificationTargetType.REMINDER,
                            targetId = occurrence.reminderId,
                            scheduledAtMillis = scheduledAtMillis,
                            occurrenceKey = occurrenceKey,
                            occurrenceKind = NotificationOccurrenceKind.SESSION
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
                AppResult.Success(
                    SessionScheduleResult(
                        sessionReminderCount = sessionReminderCount,
                        scheduledNotificationCount = notifications.size
                    )
                )
            }
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
        rescheduleMutex.withLock {
            withContext(dispatcherProvider.io) {
                cancelSessionNotifications()
            }
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

    suspend fun getElapsedSessionOccurrences(
        startedAt: LocalDateTime,
        endedAt: LocalDateTime
    ): List<ReminderOccurrence> {
        return withContext(dispatcherProvider.io) {
            val reminders = reminderRepository.getAllRemindersSnapshot()
                .filter { reminder ->
                    reminder.isEnabled && reminder.notificationEnabled
                }

            val statesByKey = reminderOccurrenceRepository.getByRange(
                startMillis = startedAt.toEpochMillis(),
                endMillis = endedAt.plus(SESSION_LOOK_AHEAD).toEpochMillis() + 1L
            ).associateBy { state -> state.occurrenceKey }

            buildSessionOccurrences(
                reminders = reminders,
                startedAt = startedAt,
                from = startedAt
            )
                .map { occurrence -> occurrence.toReminderOccurrence() }
                .map { occurrence ->
                    occurrence.copy(
                        status = statesByKey[occurrence.occurrenceKey]?.status
                            ?: ReminderOccurrenceStatus.PLANNED
                    )
                }
                .filter { occurrence -> !occurrence.scheduledAt.isAfter(endedAt) || occurrence.status == ReminderOccurrenceStatus.COMPLETED}
        }
    }

    private fun observeSessionOccurrenceStates(startedAt: LocalDateTime) =
        reminderOccurrenceRepository.observeByRange(
            startMillis = startedAt.toEpochMillis(),
            endMillis = startedAt.plus(SESSION_LOOK_AHEAD).toEpochMillis()
        )

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
