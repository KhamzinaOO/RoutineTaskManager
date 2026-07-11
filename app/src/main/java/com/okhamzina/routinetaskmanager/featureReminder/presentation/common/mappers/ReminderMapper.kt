package com.okhamzina.routinetaskmanager.featureReminder.presentation.common.mappers

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.DayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnSchedulePeriodDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.TimeWindow
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.IntervalRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.TimeWindowUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import java.time.LocalTime

fun DuringSessionPeriodRepeatUi.toDomain(): ReminderRepeatRule.DuringSessionPeriod {
    return ReminderRepeatRule.DuringSessionPeriod(
        schedule = schedule.toDomain { it.toDomain() }
    )
}

fun OnSchedulePeriodRepeatUi.toDomain(): ReminderRepeatRule.OnSchedulePeriod {
    return ReminderRepeatRule.OnSchedulePeriod(
        schedule = schedule.toDomain { it.toDomain() }
    )
}

fun OnScheduleCertainRepeatUi.toDomain(): ReminderRepeatRule.OnScheduleCertain {
    return ReminderRepeatRule.OnScheduleCertain(
        schedule = schedule.toDomain { it.toDomain() }
    )
}

fun RepeatIntervalUi.toDomain(): RepeatInterval {
    return RepeatInterval(
        value = value.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        unit = RepeatUnit.entries.toTypedArray().getOrElse(selectedUnitId) { RepeatUnit.MINUTES }
    )
}

fun IntervalRepeatUi.toDomain(): IntervalRepeat {
    return IntervalRepeat(
        interval = interval.toDomain()
    )
}

fun OnSchedulePeriodDayUi.toDomain(): OnSchedulePeriodDayRepeat {
    return OnSchedulePeriodDayRepeat(
        interval = interval.toDomain(),
        timeWindow = timeWindow.toDomain()
    )
}

fun OnScheduleCertainDayUi.toDomain(): OnScheduleCertainDayRepeat {
    return OnScheduleCertainDayRepeat(
        pickedTimes = pickedTimes
    )
}

fun TimeWindowUi.toDomain(): TimeWindow {
    return TimeWindow(
        startTime = startTime.toLocalTimeOr(LocalTime.of(9, 0)),
        endTime = endTime.toLocalTimeOr(LocalTime.of(18, 0)),
        allDayEnabled = allDayEnabled
    )
}

fun <T, R> WeeklyRepeatUi<T>.toDomain(
    valueMapper: (T) -> R
): WeeklyRepeat<R> {
    return WeeklyRepeat(
        mode = mode,
        selectedDays = selectedDays,
        defaultValue = valueMapper(defaultValue),
        advancedEntries = advancedEntries.map { entry ->
            DayRepeat(
                day = entry.day,
                enabled = entry.enabled,
                value = valueMapper(entry.value)
            )
        }
    )
}

private fun String.toLocalTimeOr(defaultValue: LocalTime): LocalTime {
    return runCatching { LocalTime.parse(this) }.getOrDefault(defaultValue)
}

private const val MIN_INTERVAL_VALUE = 1

internal fun String.toPositiveIntOrDefault(
    defaultValue: Int = MIN_INTERVAL_VALUE
): Int {
    return this
        .trim()
        .toIntOrNull()
        ?.coerceAtLeast(MIN_INTERVAL_VALUE)
        ?: defaultValue
}

internal fun Int.toRepeatUnitOrDefault(
    defaultValue: RepeatUnit = RepeatUnit.MINUTES
): RepeatUnit {
    return RepeatUnit.entries.getOrElse(this) {
        defaultValue
    }
}

internal fun parseLocalTimeOrDefault(
    value: String,
    defaultValue: LocalTime
): LocalTime {
    return runCatching {
        LocalTime.parse(value)
    }.getOrDefault(defaultValue)
}

internal fun parseHourMinuteOrNull(
    hours: String,
    minutes: String
): LocalTime? {
    val hour = hours.trim().toIntOrNull()
    val minute = minutes.trim().toIntOrNull()

    if (hour == null || minute == null) return null
    if (hour !in 0..23) return null
    if (minute !in 0..59) return null

    return LocalTime.of(hour, minute)
}

fun DuringSessionPeriodRepeatUi.toRepeatRule(): ReminderRepeatRule {
    return ReminderRepeatRule.DuringSessionPeriod(
        schedule = schedule.toDomain { intervalRepeatUi ->
            intervalRepeatUi.toDomain()
        }
    )
}

fun OnSchedulePeriodRepeatUi.toRepeatRule(): ReminderRepeatRule {
    return ReminderRepeatRule.OnSchedulePeriod(
        schedule = schedule.toDomain { dayUi ->
            dayUi.toDomain()
        }
    )
}

fun OnScheduleCertainRepeatUi.toRepeatRule(): ReminderRepeatRule {
    return ReminderRepeatRule.OnScheduleCertain(
        schedule = schedule.toDomain { dayUi ->
            dayUi.toDomain()
        }
    )
}
