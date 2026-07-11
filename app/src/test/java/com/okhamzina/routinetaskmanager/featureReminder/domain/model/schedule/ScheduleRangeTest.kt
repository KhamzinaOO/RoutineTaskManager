package com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleRangeTest {

    @Test
    fun dayRange_coversOneCalendarDay() {
        val date = LocalDate.of(2026, 7, 6)

        val range = dayRange(date)

        assertEquals(LocalDateTime.of(2026, 7, 6, 0, 0), range.start)
        assertEquals(LocalDateTime.of(2026, 7, 7, 0, 0), range.endExclusive)
    }

    @Test
    fun weekRange_coversSevenDaysFromStartDate() {
        val start = LocalDate.of(2026, 7, 6)

        val range = weekRange(start)

        assertEquals(start.atStartOfDay(), range.start)
        assertEquals(start.plusWeeks(1).atStartOfDay(), range.endExclusive)
    }

    @Test
    fun monthRange_startsAtFirstDayAndEndsAtNextMonth() {
        val range = monthRange(LocalDate.of(2026, 7, 19))

        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), range.start)
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0), range.endExclusive)
    }

    @Test
    fun yearRange_coversWholeYear() {
        val range = yearRange(2026)

        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), range.start)
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0), range.endExclusive)
    }
}
