package com.example.routinetaskmanager.featureReminder.presentation.common.mappers

import com.example.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.domain.model.TimeWindow
import com.example.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.TimeWindowUi
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderRepeatMapperTest {

    @Test
    fun repeatIntervalUiToDomain_coercesInvalidValueAndUnitToDefaults() {
        val interval = RepeatIntervalUi(
            value = "0",
            selectedUnitId = 99
        ).toDomain()

        assertEquals(1, interval.value)
        assertEquals(RepeatUnit.MINUTES, interval.unit)
    }

    @Test
    fun timeWindowUiToDomain_usesDefaultTimesWhenInputIsInvalid() {
        val window = TimeWindowUi(
            startTime = "bad",
            endTime = "also bad",
            allDayEnabled = true
        ).toDomain()

        assertEquals(LocalTime.of(9, 0), window.startTime)
        assertEquals(LocalTime.of(18, 0), window.endTime)
        assertEquals(true, window.allDayEnabled)
    }

    @Test
    fun onScheduleCertainDayUiToDomain_preservesPickedTimes() {
        val pickedTimes = setOf(LocalTime.of(8, 10), LocalTime.of(17, 45))

        val dayRepeat = OnScheduleCertainDayUi(
            hours = "09",
            minutes = "00",
            pickedTimes = pickedTimes
        ).toDomain()

        assertEquals(pickedTimes, dayRepeat.pickedTimes)
    }

    @Test
    fun domainToUi_formatsTimeWithLeadingZeroes() {
        val ui = TimeWindow(
            startTime = LocalTime.of(7, 5),
            endTime = LocalTime.of(18, 0),
            allDayEnabled = false
        ).toUi()

        assertEquals("07:05", ui.startTime)
        assertEquals("18:00", ui.endTime)
        assertEquals(false, ui.allDayEnabled)
    }

    @Test
    fun toUiStateBundle_setsActiveRepeatTypeAndCopiesDuringSessionState() {
        val rule = ReminderRepeatRule.DuringSessionPeriod(
            schedule = WeeklyRepeat(
                mode = RepeatScheduleMode.DEFAULT,
                selectedDays = setOf(DayOfWeek.MONDAY),
                defaultValue = IntervalRepeat(
                    interval = RepeatInterval(3, RepeatUnit.DAYS)
                ),
                advancedEntries = emptyList()
            )
        )

        val bundle = rule.toUiStateBundle()

        assertEquals(ReminderRepeatType.DURING_SESSION_PERIOD, bundle.repeatType)
        assertEquals(
            RepeatIntervalUi(
                value = "3",
                selectedUnitId = RepeatUnit.DAYS.ordinal
            ),
            bundle.duringSessionState.schedule.defaultValue.interval
        )
    }
}
