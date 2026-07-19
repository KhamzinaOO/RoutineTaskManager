package com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnSchedulePeriodDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderOccurrenceKeyFactory
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.type
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduleCalculator {

    fun buildOccurrences(
        reminders: List<Reminder>,
        range: ScheduleRange
    ): List<ReminderOccurrence> {
        return generateOccurrences(reminders, range)
            .sortedBy { it.scheduledAt }
            .toList()
    }

    fun generateOccurrences(
        reminders: List<Reminder>,
        range: ScheduleRange
    ): Sequence<ReminderOccurrence> {
        return reminders.asSequence().flatMap { reminder ->
            generateOccurrencesForReminder(
                reminder = reminder,
                range = range
            )
        }
    }

    fun buildOccurrencesForReminder(
        reminder: Reminder,
        range: ScheduleRange
    ): List<ReminderOccurrence> {
        return generateOccurrencesForReminder(reminder, range).toList()
    }

    private fun generateOccurrencesForReminder(
        reminder: Reminder,
        range: ScheduleRange
    ): Sequence<ReminderOccurrence> {
        return when (val rule = reminder.repeatRule) {
            is ReminderRepeatRule.OnSchedulePeriod -> {
                generateOnSchedulePeriodOccurrences(
                    reminder = reminder,
                    rule = rule,
                    range = range
                )
            }

            is ReminderRepeatRule.OnScheduleCertain -> {
                generateOnScheduleCertainOccurrences(
                    reminder = reminder,
                    rule = rule,
                    range = range
                )
            }

            is ReminderRepeatRule.DuringSessionPeriod -> {
                emptySequence()
            }
        }
    }

    private fun generateOnScheduleCertainOccurrences(
        reminder: Reminder,
        rule: ReminderRepeatRule.OnScheduleCertain,
        range: ScheduleRange
    ): Sequence<ReminderOccurrence> = sequence {
        datesInRange(range).forEach { date ->
            val dayOfWeek = date.dayOfWeek

            val dayValue = when (rule.schedule.mode) {
                RepeatScheduleMode.DEFAULT -> {
                    if (dayOfWeek !in rule.schedule.selectedDays) {
                        null
                    } else {
                        rule.schedule.defaultValue
                    }
                }

                RepeatScheduleMode.ADVANCED -> {
                    rule.schedule.advancedEntries
                        .firstOrNull { it.day == dayOfWeek && it.enabled }
                        ?.value
                }
            }

            dayValue?.pickedTimes?.forEach { time ->
                val scheduledAt = date.atTime(time)

                if (scheduledAt in range) {
                    yield(
                        reminder.toOccurrence(
                            scheduledAt = scheduledAt
                        )
                    )
                }
            }
        }
    }

    private fun generateOnSchedulePeriodOccurrences(
        reminder: Reminder,
        rule: ReminderRepeatRule.OnSchedulePeriod,
        range: ScheduleRange
    ): Sequence<ReminderOccurrence> = sequence {
        datesInRange(range).forEach { date ->
            val dayOfWeek = date.dayOfWeek

            val dayValue = when (rule.schedule.mode) {
                RepeatScheduleMode.DEFAULT -> {
                    if (dayOfWeek !in rule.schedule.selectedDays) {
                        null
                    } else {
                        rule.schedule.defaultValue
                    }
                }

                RepeatScheduleMode.ADVANCED -> {
                    rule.schedule.advancedEntries
                        .firstOrNull { it.day == dayOfWeek && it.enabled }
                        ?.value
                }
            }

            if (dayValue != null) {
                yieldAll(
                    generatePeriodOccurrencesForDate(
                        reminder = reminder,
                        date = date,
                        dayRepeat = dayValue,
                        range = range
                    )
                )
            }
        }
    }

    private fun generatePeriodOccurrencesForDate(
        reminder: Reminder,
        date: LocalDate,
        dayRepeat: OnSchedulePeriodDayRepeat,
        range: ScheduleRange
    ): Sequence<ReminderOccurrence> = sequence {
        val timeWindow = dayRepeat.timeWindow

        val startTime = if (timeWindow.allDayEnabled) {
            LocalTime.MIN
        } else {
            timeWindow.startTime
        }

        val endTime = if (timeWindow.allDayEnabled) {
            LocalTime.of(23, 59)
        } else {
            timeWindow.endTime
        }

        if (!timeWindow.allDayEnabled && !startTime.isBefore(endTime)) {
            return@sequence
        }

        val step = dayRepeat.interval.toDurationOrNull()
            ?: return@sequence

        var current = date.atTime(startTime)
        val end = date.atTime(endTime)
        var generatedCount = 0

        while (
            !current.isAfter(end) &&
            generatedCount < MAX_OCCURRENCES_PER_REMINDER_PER_DAY
        ) {
            if (current in range) {
                yield(
                    reminder.toOccurrence(
                        scheduledAt = current
                    )
                )
                generatedCount++
            }

            current = current.plus(step)
        }
    }

    private fun datesInRange(
        range: ScheduleRange
    ): Sequence<LocalDate> = sequence {
        if (!range.start.isBefore(range.endExclusive)) return@sequence

        var currentDate = range.start.toLocalDate()
        val lastDate = range.endExclusive.minusNanos(1).toLocalDate()

        while (!currentDate.isAfter(lastDate)) {
            yield(currentDate)
            currentDate = currentDate.plusDays(1)
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

    private fun Reminder.toOccurrence(
        scheduledAt: LocalDateTime
    ): ReminderOccurrence {
        val scheduledAtMillis = scheduledAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return ReminderOccurrence(
            reminderId = id,
            reminderName = name,
            instructionsText = instructionsText,
            scheduledAt = scheduledAt,
            repeatType = repeatRule.type,
            occurrenceKey = ReminderOccurrenceKeyFactory.regular(
                reminderId = id,
                scheduledAtMillis = scheduledAtMillis
            ),
            scheduledAtMillis = scheduledAtMillis
        )
    }

    private operator fun ScheduleRange.contains(
        dateTime: LocalDateTime
    ): Boolean {
        return !dateTime.isBefore(start) && dateTime.isBefore(endExclusive)
    }

    private companion object {
        const val MAX_OCCURRENCES_PER_REMINDER_PER_DAY = 10_000
    }
}


