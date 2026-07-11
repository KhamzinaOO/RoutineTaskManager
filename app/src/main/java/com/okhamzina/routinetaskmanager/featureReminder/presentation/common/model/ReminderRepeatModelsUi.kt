package com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import java.time.DayOfWeek
import java.time.LocalTime
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

fun <T> defaultWeeklyRepeatUi(
    defaultValue: T,
    selectedDays: Set<DayOfWeek> = emptySet(),
    advancedValueFactory: (DayOfWeek) -> T = { defaultValue }
): WeeklyRepeatUi<T> {
    return WeeklyRepeatUi(
        selectedDays = selectedDays,
        defaultValue = defaultValue,
        advancedEntries = DayOfWeek.entries.map { day ->
            DayRepeatUi(
                day = day,
                enabled = day in selectedDays,
                value = advancedValueFactory(day)
            )
        }
    )
}

data class ReminderRepeatUiStateBundle(
    val repeatType: ReminderRepeatType,
    val onSchedulePeriodState: OnSchedulePeriodRepeatUi,
    val onScheduleCertainState: OnScheduleCertainRepeatUi,
    val duringSessionState: DuringSessionPeriodRepeatUi
)
