package com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.DayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnSchedulePeriodDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.Reminder
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.TimeWindow
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderScheduleCalculatorTest {

    private val calculator = ReminderScheduleCalculator()
    private val monday = LocalDate.of(2026, 7, 6)

    @Test
    fun buildOccurrencesForReminder_onSchedulePeriod_returnsIntervalOccurrencesInsideWindow() {
        val reminder = reminder(
            rule = ReminderRepeatRule.OnSchedulePeriod(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.MONDAY),
                    defaultValue = OnSchedulePeriodDayRepeat(
                        interval = RepeatInterval(30, RepeatUnit.MINUTES),
                        timeWindow = TimeWindow(
                            startTime = LocalTime.of(9, 0),
                            endTime = LocalTime.of(10, 0),
                            allDayEnabled = false
                        )
                    ),
                    advancedEntries = emptyList()
                )
            )
        )

        val occurrences = calculator.buildOccurrencesForReminder(
            reminder = reminder,
            range = ScheduleRange(
                start = monday.atTime(8, 0),
                endExclusive = monday.atTime(11, 0)
            )
        )

        assertEquals(
            listOf(LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0)),
            occurrences.map { it.scheduledAt.toLocalTime() }
        )
        assertEquals(
            listOf(ReminderRepeatType.ON_SCHEDULE_PERIOD),
            occurrences.map { it.repeatType }.distinct()
        )
    }

    @Test
    fun buildOccurrencesForReminder_onScheduleCertain_excludesRangeEnd() {
        val reminder = reminder(
            rule = ReminderRepeatRule.OnScheduleCertain(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.MONDAY),
                    defaultValue = OnScheduleCertainDayRepeat(
                        pickedTimes = setOf(LocalTime.of(10, 0))
                    ),
                    advancedEntries = emptyList()
                )
            )
        )

        val occurrences = calculator.buildOccurrencesForReminder(
            reminder = reminder,
            range = ScheduleRange(
                start = monday.atTime(9, 0),
                endExclusive = monday.atTime(10, 0)
            )
        )

        assertTrue(occurrences.isEmpty())
    }

    @Test
    fun buildOccurrencesForReminder_defaultMode_ignoresDaysThatAreNotSelected() {
        val reminder = reminder(
            rule = ReminderRepeatRule.OnScheduleCertain(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.TUESDAY),
                    defaultValue = OnScheduleCertainDayRepeat(
                        pickedTimes = setOf(LocalTime.of(9, 0))
                    ),
                    advancedEntries = emptyList()
                )
            )
        )

        val occurrences = calculator.buildOccurrencesForReminder(
            reminder = reminder,
            range = dayRange(monday)
        )

        assertTrue(occurrences.isEmpty())
    }

    @Test
    fun buildOccurrencesForReminder_advancedMode_usesOnlyEnabledDayEntries() {
        val reminder = reminder(
            rule = ReminderRepeatRule.OnScheduleCertain(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.ADVANCED,
                    selectedDays = emptySet(),
                    defaultValue = OnScheduleCertainDayRepeat(
                        pickedTimes = setOf(LocalTime.of(12, 0))
                    ),
                    advancedEntries = listOf(
                        DayRepeat(
                            day = DayOfWeek.MONDAY,
                            enabled = true,
                            value = OnScheduleCertainDayRepeat(
                                pickedTimes = setOf(LocalTime.of(8, 15))
                            )
                        ),
                        DayRepeat(
                            day = DayOfWeek.TUESDAY,
                            enabled = false,
                            value = OnScheduleCertainDayRepeat(
                                pickedTimes = setOf(LocalTime.of(9, 45))
                            )
                        )
                    )
                )
            )
        )

        val occurrences = calculator.buildOccurrencesForReminder(
            reminder = reminder,
            range = ScheduleRange(
                start = monday.atStartOfDay(),
                endExclusive = monday.plusDays(2).atStartOfDay()
            )
        )

        assertEquals(
            listOf(LocalDateTime.of(monday, LocalTime.of(8, 15))),
            occurrences.map { it.scheduledAt }
        )
    }

    @Test
    fun buildOccurrencesForReminder_periodWithInvalidWindow_returnsEmptyList() {
        val reminder = reminder(
            rule = ReminderRepeatRule.OnSchedulePeriod(
                schedule = WeeklyRepeat(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.MONDAY),
                    defaultValue = OnSchedulePeriodDayRepeat(
                        interval = RepeatInterval(1, RepeatUnit.HOURS),
                        timeWindow = TimeWindow(
                            startTime = LocalTime.of(18, 0),
                            endTime = LocalTime.of(9, 0),
                            allDayEnabled = false
                        )
                    ),
                    advancedEntries = emptyList()
                )
            )
        )

        val occurrences = calculator.buildOccurrencesForReminder(
            reminder = reminder,
            range = dayRange(monday)
        )

        assertTrue(occurrences.isEmpty())
    }

    @Test
    fun buildOccurrences_sortsOccurrencesFromDifferentRemindersByScheduledTime() {
        val lateReminder = reminder(
            id = 1,
            rule = certainRule(time = LocalTime.of(11, 0))
        )
        val earlyReminder = reminder(
            id = 2,
            rule = certainRule(time = LocalTime.of(9, 0))
        )

        val occurrences = calculator.buildOccurrences(
            reminders = listOf(lateReminder, earlyReminder),
            range = dayRange(monday)
        )

        assertEquals(listOf(2L, 1L), occurrences.map { it.reminderId })
    }

    private fun certainRule(time: LocalTime): ReminderRepeatRule.OnScheduleCertain {
        return ReminderRepeatRule.OnScheduleCertain(
            schedule = WeeklyRepeat(
                mode = RepeatScheduleMode.DEFAULT,
                selectedDays = setOf(DayOfWeek.MONDAY),
                defaultValue = OnScheduleCertainDayRepeat(
                    pickedTimes = setOf(time)
                ),
                advancedEntries = emptyList()
            )
        )
    }

    private fun reminder(
        id: Long = 1,
        rule: ReminderRepeatRule
    ): Reminder {
        return Reminder(
            id = id,
            name = "Reminder $id",
            instructionsText = "Instruction",
            repeatRule = rule,
            notificationMode = NotificationMode.MUTE,
            createdAt = 1L,
            updatedAt = 1L
        )
    }
}
