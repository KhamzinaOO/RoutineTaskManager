package com.okhamzina.routinetaskmanager.featureReminder.domain.model

import com.okhamzina.routinetaskmanager.core.serialization.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class RepeatInterval(
    val value: Int,
    val unit: RepeatUnit
)

@Serializable
data class TimeWindow(
    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime,
    @Serializable(with = LocalTimeSerializer::class)
    val endTime: LocalTime,
    val allDayEnabled: Boolean
)

@Serializable
data class IntervalRepeat(
    val interval: RepeatInterval
)

@Serializable
data class OnSchedulePeriodDayRepeat(
    val interval: RepeatInterval,
    val timeWindow: TimeWindow
)

@Serializable
data class OnScheduleCertainDayRepeat(
    val pickedTimes: Set<
            @Serializable(with = LocalTimeSerializer::class)
            LocalTime
            >
)

@Serializable
data class DayRepeat<T>(
    val day: DayOfWeek,
    val enabled: Boolean,
    val value: T
)

@Serializable
data class WeeklyRepeat<T>(
    val mode: RepeatScheduleMode,
    val selectedDays: Set<DayOfWeek>,
    val defaultValue: T,
    val advancedEntries: List<DayRepeat<T>>
)

@Serializable
sealed interface ReminderRepeatRule {
    @Serializable
    data class DuringSessionPeriod(
        val schedule: WeeklyRepeat<IntervalRepeat>
    ) : ReminderRepeatRule
    @Serializable
    data class OnSchedulePeriod(
        val schedule: WeeklyRepeat<OnSchedulePeriodDayRepeat>
    ) : ReminderRepeatRule
    @Serializable
    data class OnScheduleCertain(
        val schedule: WeeklyRepeat<OnScheduleCertainDayRepeat>
    ) : ReminderRepeatRule
}

