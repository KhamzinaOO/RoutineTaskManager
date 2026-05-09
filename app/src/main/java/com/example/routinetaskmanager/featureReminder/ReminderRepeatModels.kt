package com.example.routinetaskmanager.featureReminder

import java.time.DayOfWeek
import java.time.LocalTime

enum class RepeatScheduleMode {
    DEFAULT,
    ADVANCED
}

enum class RepeatUnit {
    MINUTES,
    HOURS,
    DAYS
}

enum class ReminderRepeatType {
    ON_SCHEDULE_PERIOD,
    ON_SCHEDULE_CERTAIN,
    DURING_SESSION_PERIOD,
    AFTER_ANOTHER_ACTIVITY
}

data class RepeatIntervalUi(
    val value: String = "1",
    val selectedUnitId: Int = RepeatUnit.MINUTES.ordinal
)

data class TimeWindowUi(
    val startTime: String = "09:00",
    val endTime: String = "18:00",
    val allDayEnabled: Boolean = false
)

data class IntervalRepeatUi(
    val interval: RepeatIntervalUi = RepeatIntervalUi()
)

data class OnSchedulePeriodDayUi(
    val interval: RepeatIntervalUi = RepeatIntervalUi(),
    val timeWindow: TimeWindowUi = TimeWindowUi()
)

data class OnScheduleCertainDayUi(
    val hours: String = "09",
    val minutes: String = "00",
    val pickedTimes: Set<LocalTime> = emptySet()
)

data class DayRepeatUi<T>(
    val day: DayOfWeek,
    val enabled: Boolean = false,
    val value: T
)

data class WeeklyRepeatUi<T>(
    val mode: RepeatScheduleMode = RepeatScheduleMode.DEFAULT,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val defaultValue: T,
    val advancedEntries: List<DayRepeatUi<T>>
)

data class AfterAnotherRepeatUi(
    val waitInterval: RepeatIntervalUi = RepeatIntervalUi()
)

data class DuringSessionPeriodRepeatUi(
    val schedule: WeeklyRepeatUi<IntervalRepeatUi> = defaultWeeklyRepeatUi(
        defaultValue = IntervalRepeatUi()
    )
)

data class OnSchedulePeriodRepeatUi(
    val schedule: WeeklyRepeatUi<OnSchedulePeriodDayUi> = defaultWeeklyRepeatUi(
        defaultValue = OnSchedulePeriodDayUi()
    )
)

data class OnScheduleCertainRepeatUi(
    val schedule: WeeklyRepeatUi<OnScheduleCertainDayUi> = defaultWeeklyRepeatUi(
        defaultValue = OnScheduleCertainDayUi()
    )
)

data class RepeatInterval(
    val value: Int,
    val unit: RepeatUnit
)

data class TimeWindow(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val allDayEnabled: Boolean
)

data class IntervalRepeat(
    val interval: RepeatInterval
)

data class OnSchedulePeriodDayRepeat(
    val interval: RepeatInterval,
    val timeWindow: TimeWindow
)

data class OnScheduleCertainDayRepeat(
    val pickedTimes: Set<LocalTime>
)

data class DayRepeat<T>(
    val day: DayOfWeek,
    val enabled: Boolean,
    val value: T
)

data class WeeklyRepeat<T>(
    val mode: RepeatScheduleMode,
    val selectedDays: Set<DayOfWeek>,
    val defaultValue: T,
    val advancedEntries: List<DayRepeat<T>>
)

sealed interface ReminderRepeatRule {
    data class AfterAnother(
        val waitInterval: RepeatInterval
    ) : ReminderRepeatRule

    data class DuringSessionPeriod(
        val schedule: WeeklyRepeat<IntervalRepeat>
    ) : ReminderRepeatRule

    data class OnSchedulePeriod(
        val schedule: WeeklyRepeat<OnSchedulePeriodDayRepeat>
    ) : ReminderRepeatRule

    data class OnScheduleCertain(
        val schedule: WeeklyRepeat<OnScheduleCertainDayRepeat>
    ) : ReminderRepeatRule
}

fun <T> defaultWeeklyRepeatUi(
    defaultValue: T,
    selectedDays: Set<DayOfWeek> = emptySet(),
    advancedValueFactory: (DayOfWeek) -> T = { defaultValue }
): WeeklyRepeatUi<T> {
    return WeeklyRepeatUi(
        selectedDays = selectedDays,
        defaultValue = defaultValue,
        advancedEntries = DayOfWeek.values().map { day ->
            DayRepeatUi(
                day = day,
                enabled = day in selectedDays,
                value = advancedValueFactory(day)
            )
        }
    )
}

fun AfterAnotherRepeatUi.toDomain(): ReminderRepeatRule.AfterAnother {
    return ReminderRepeatRule.AfterAnother(
        waitInterval = waitInterval.toDomain()
    )
}

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
        unit = RepeatUnit.values().getOrElse(selectedUnitId) { RepeatUnit.MINUTES }
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
