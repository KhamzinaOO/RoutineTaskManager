package com.example.routinetaskmanager.featureReminder.domain.model.schedule

import com.example.routinetaskmanager.featureReminder.domain.model.OnSchedulePeriodDayRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderOccurrence
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.domain.model.type
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderScheduleCalculator {

    fun buildOccurrences(
        reminders: List<Reminder>,
        range: ScheduleRange
    ): List<ReminderOccurrence> {
        return reminders
            .flatMap { reminder ->
                buildOccurrencesForReminder(
                    reminder = reminder,
                    range = range
                )
            }
            .sortedBy { it.scheduledAt }
    }

    fun buildOccurrencesForReminder(
        reminder: Reminder,
        range: ScheduleRange
    ): List<ReminderOccurrence> {
        return when (val rule = reminder.repeatRule) {
            is ReminderRepeatRule.OnSchedulePeriod -> {
                buildOnSchedulePeriodOccurrences(
                    reminder = reminder,
                    rule = rule,
                    range = range
                )
            }

            is ReminderRepeatRule.OnScheduleCertain -> {
                buildOnScheduleCertainOccurrences(
                    reminder = reminder,
                    rule = rule,
                    range = range
                )
            }

            is ReminderRepeatRule.DuringSessionPeriod -> {
                emptyList()
            }

            is ReminderRepeatRule.AfterAnother -> {
                emptyList()
            }
        }
    }

    private fun buildOnScheduleCertainOccurrences(
        reminder: Reminder,
        rule: ReminderRepeatRule.OnScheduleCertain,
        range: ScheduleRange
    ): List<ReminderOccurrence> {
        val result = mutableListOf<ReminderOccurrence>()

        forEachDateInRange(range) { date ->
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
                    result.add(
                        reminder.toOccurrence(
                            scheduledAt = scheduledAt
                        )
                    )
                }
            }
        }

        return result
    }

    private fun buildOnSchedulePeriodOccurrences(
        reminder: Reminder,
        rule: ReminderRepeatRule.OnSchedulePeriod,
        range: ScheduleRange
    ): List<ReminderOccurrence> {
        val result = mutableListOf<ReminderOccurrence>()

        forEachDateInRange(range) { date ->
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
                result.addAll(
                    buildPeriodOccurrencesForDate(
                        reminder = reminder,
                        date = date,
                        dayRepeat = dayValue,
                        range = range
                    )
                )
            }
        }

        return result
    }

    private fun buildPeriodOccurrencesForDate(
        reminder: Reminder,
        date: LocalDate,
        dayRepeat: OnSchedulePeriodDayRepeat,
        range: ScheduleRange
    ): List<ReminderOccurrence> {
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
            return emptyList()
        }

        val step = dayRepeat.interval.toDurationOrNull()
            ?: return emptyList()

        val result = mutableListOf<ReminderOccurrence>()

        var current = date.atTime(startTime)
        val end = date.atTime(endTime)

        while (!current.isAfter(end)) {
            if (current in range) {
                result.add(
                    reminder.toOccurrence(
                        scheduledAt = current
                    )
                )
            }

            current = current.plus(step)

            if (result.size > MAX_OCCURRENCES_PER_REMINDER_IN_RANGE) {
                break
            }
        }

        return result
    }

    private fun forEachDateInRange(
        range: ScheduleRange,
        action: (LocalDate) -> Unit
    ) {
        if (!range.start.isBefore(range.endExclusive)) return

        var currentDate = range.start.toLocalDate()
        val lastDate = range.endExclusive.minusNanos(1).toLocalDate()

        while (!currentDate.isAfter(lastDate)) {
            action(currentDate)
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
        return ReminderOccurrence(
            reminderId = id,
            reminderName = name,
            instructionsText = instructionsText,
            scheduledAt = scheduledAt,
            repeatType = repeatRule.type
        )
    }

    private operator fun ScheduleRange.contains(
        dateTime: LocalDateTime
    ): Boolean {
        return !dateTime.isBefore(start) && dateTime.isBefore(endExclusive)
    }

    private companion object {
        const val MAX_OCCURRENCES_PER_REMINDER_IN_RANGE = 10_000
    }
}


