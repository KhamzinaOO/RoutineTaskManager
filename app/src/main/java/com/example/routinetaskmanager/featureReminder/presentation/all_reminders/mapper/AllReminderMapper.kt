package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.mapper

import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderMiniCardUi
import java.time.DayOfWeek

fun Reminder.toMiniCardUi(): ReminderMiniCardUi {
    return ReminderMiniCardUi(
        id = id,
        title = name,
        repeatLabel = repeatRule.toRepeatLabel(),
        detailsLabel = repeatRule.toShortDetailsLabel(),
        notificationMode = notificationMode,
        notificationEnabled = notificationEnabled,
        isEnabled = isEnabled,
    )
}

private fun ReminderRepeatRule.toRepeatLabel(): String {
    return when (this) {
        is ReminderRepeatRule.AfterAnother -> {
            "After another reminder"
        }

        is ReminderRepeatRule.DuringSessionPeriod -> {
            "During session"
        }

        is ReminderRepeatRule.OnSchedulePeriod -> {
            "On schedule · Period"
        }

        is ReminderRepeatRule.OnScheduleCertain -> {
            "On schedule · Certain time"
        }
    }
}

private fun ReminderRepeatRule.toShortDetailsLabel(): String? {
    return when (this) {
        is ReminderRepeatRule.AfterAnother -> {
            "After ${waitInterval.value} ${waitInterval.unit.toLabel(waitInterval.value)}"
        }

        is ReminderRepeatRule.DuringSessionPeriod -> {
            val days = schedule.selectedDays.toShortDaysLabel()
            val interval = schedule.defaultValue.interval

            "Every ${interval.value} ${interval.unit.toLabel(interval.value)} · $days"
        }

        is ReminderRepeatRule.OnSchedulePeriod -> {
            val days = schedule.selectedDays.toShortDaysLabel()
            val interval = schedule.defaultValue.interval

            "Every ${interval.value} ${interval.unit.toLabel(interval.value)} · $days"
        }

        is ReminderRepeatRule.OnScheduleCertain -> {
            val days = schedule.selectedDays.toShortDaysLabel()
            val timesCount = schedule.defaultValue.pickedTimes.size

            when (timesCount) {
                0 -> days
                1 -> "1 time · $days"
                else -> "$timesCount times · $days"
            }
        }
    }
}

private fun Set<DayOfWeek>.toShortDaysLabel(): String {
    if (isEmpty()) return "Never"

    if (size == DayOfWeek.entries.size) {
        return "Every day"
    }

    val weekdays = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    )

    val weekend = setOf(
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    if (this == weekdays) return "Weekdays"
    if (this == weekend) return "Weekend"

    return sortedBy { it.value }
        .joinToString(", ") { day ->
            day.toShortLabel()
        }
}

private fun DayOfWeek.toShortLabel(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}

private fun RepeatUnit.toLabel(
    value: Int
): String {
    return when (this) {
        RepeatUnit.MINUTES -> if (value == 1) "minute" else "minutes"
        RepeatUnit.HOURS -> if (value == 1) "hour" else "hours"
        RepeatUnit.DAYS -> if (value == 1) "day" else "days"
    }
}