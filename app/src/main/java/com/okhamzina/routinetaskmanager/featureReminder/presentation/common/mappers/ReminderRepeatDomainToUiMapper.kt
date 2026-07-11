package com.okhamzina.routinetaskmanager.featureReminder.presentation.common.mappers

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.IntervalRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnScheduleCertainDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.OnSchedulePeriodDayRepeat
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatInterval
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.TimeWindow
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.WeeklyRepeat
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.DayRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.IntervalRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.ReminderRepeatUiStateBundle
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.TimeWindowUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import java.time.LocalTime

fun RepeatInterval.toUi(): RepeatIntervalUi {
    return RepeatIntervalUi(
        value = value.toString(),
        selectedUnitId = unit.ordinal
    )
}

fun TimeWindow.toUi(): TimeWindowUi {
    return TimeWindowUi(
        startTime = startTime.toUiTimeString(),
        endTime = endTime.toUiTimeString(),
        allDayEnabled = allDayEnabled
    )
}

private fun LocalTime.toUiTimeString(): String {
    return "%02d:%02d".format(hour, minute)
}

fun IntervalRepeat.toUi(): IntervalRepeatUi {
    return IntervalRepeatUi(
        interval = interval.toUi()
    )
}

fun OnSchedulePeriodDayRepeat.toUi(): OnSchedulePeriodDayUi {
    return OnSchedulePeriodDayUi(
        interval = interval.toUi(),
        timeWindow = timeWindow.toUi()
    )
}

fun OnScheduleCertainDayRepeat.toUi(): OnScheduleCertainDayUi {
    val firstTime = pickedTimes.firstOrNull() ?: LocalTime.of(9, 0)

    return OnScheduleCertainDayUi(
        hours = "%02d".format(firstTime.hour),
        minutes = "%02d".format(firstTime.minute),
        pickedTimes = pickedTimes
    )
}

fun <DomainValue, UiValue> WeeklyRepeat<DomainValue>.toUi(
    valueMapper: (DomainValue) -> UiValue
): WeeklyRepeatUi<UiValue> {
    return WeeklyRepeatUi(
        mode = mode,
        selectedDays = selectedDays,
        defaultValue = valueMapper(defaultValue),
        advancedEntries = advancedEntries.map { dayRepeat ->
            DayRepeatUi(
                day = dayRepeat.day,
                enabled = dayRepeat.enabled,
                value = valueMapper(dayRepeat.value)
            )
        }
    )
}

fun ReminderRepeatRule.DuringSessionPeriod.toUi(): DuringSessionPeriodRepeatUi {
    return DuringSessionPeriodRepeatUi(
        schedule = schedule.toUi { intervalRepeat ->
            intervalRepeat.toUi()
        }
    )
}

fun ReminderRepeatRule.OnSchedulePeriod.toUi(): OnSchedulePeriodRepeatUi {
    return OnSchedulePeriodRepeatUi(
        schedule = schedule.toUi { dayRepeat ->
            dayRepeat.toUi()
        }
    )
}

fun ReminderRepeatRule.OnScheduleCertain.toUi(): OnScheduleCertainRepeatUi {
    return OnScheduleCertainRepeatUi(
        schedule = schedule.toUi { dayRepeat ->
            dayRepeat.toUi()
        }
    )
}

fun ReminderRepeatRule.toUiStateBundle(): ReminderRepeatUiStateBundle {
    return when (this) {
        is ReminderRepeatRule.OnSchedulePeriod -> {
            ReminderRepeatUiStateBundle(
                repeatType = ReminderRepeatType.ON_SCHEDULE_PERIOD,
                onSchedulePeriodState = this.toUi(),
                onScheduleCertainState = OnScheduleCertainRepeatUi(),
                duringSessionState = DuringSessionPeriodRepeatUi()
            )
        }

        is ReminderRepeatRule.OnScheduleCertain -> {
            ReminderRepeatUiStateBundle(
                repeatType = ReminderRepeatType.ON_SCHEDULE_CERTAIN,
                onSchedulePeriodState = OnSchedulePeriodRepeatUi(),
                onScheduleCertainState = this.toUi(),
                duringSessionState = DuringSessionPeriodRepeatUi()
            )
        }

        is ReminderRepeatRule.DuringSessionPeriod -> {
            ReminderRepeatUiStateBundle(
                repeatType = ReminderRepeatType.DURING_SESSION_PERIOD,
                onSchedulePeriodState = OnSchedulePeriodRepeatUi(),
                onScheduleCertainState = OnScheduleCertainRepeatUi(),
                duringSessionState = this.toUi()
            )
        }
    }
}
