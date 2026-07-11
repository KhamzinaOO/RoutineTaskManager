package com.okhamzina.routinetaskmanager.featureReminder.domain.model.schedule

import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleRange(
    val start: LocalDateTime,
    val endExclusive: LocalDateTime
)

fun dayRange(date: LocalDate): ScheduleRange {
    return ScheduleRange(
        start = date.atStartOfDay(),
        endExclusive = date.plusDays(1).atStartOfDay()
    )
}

fun weekRange(startOfWeek: LocalDate): ScheduleRange {
    return ScheduleRange(
        start = startOfWeek.atStartOfDay(),
        endExclusive = startOfWeek.plusWeeks(1).atStartOfDay()
    )
}

fun monthRange(monthDate: LocalDate): ScheduleRange {
    val start = monthDate.withDayOfMonth(1)

    return ScheduleRange(
        start = start.atStartOfDay(),
        endExclusive = start.plusMonths(1).atStartOfDay()
    )
}

fun yearRange(year: Int): ScheduleRange {
    val start = LocalDate.of(year, 1, 1)

    return ScheduleRange(
        start = start.atStartOfDay(),
        endExclusive = start.plusYears(1).atStartOfDay()
    )
}