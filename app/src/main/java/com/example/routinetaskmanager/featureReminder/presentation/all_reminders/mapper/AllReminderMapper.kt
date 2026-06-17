package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderMiniCardUi
import java.time.DayOfWeek
import java.time.format.TextStyle

@Composable
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

@Composable
private fun ReminderRepeatRule.toRepeatLabel(): String {
    return when (this) {
        is ReminderRepeatRule.AfterAnother -> {
            stringResource(R.string.repeat_label_after_another_reminder)
        }

        is ReminderRepeatRule.DuringSessionPeriod -> {
            stringResource(R.string.repeat_type_during_session)
        }

        is ReminderRepeatRule.OnSchedulePeriod -> {
            stringResource(R.string.repeat_label_on_schedule_period_compact)
        }

        is ReminderRepeatRule.OnScheduleCertain -> {
            stringResource(R.string.repeat_label_on_schedule_certain_compact)
        }
    }
}

@Composable
private fun ReminderRepeatRule.toShortDetailsLabel(): String? {
    return when (this) {
        is ReminderRepeatRule.AfterAnother -> {
            stringResource(
                R.string.repeat_detail_after,
                waitInterval.value,
                waitInterval.unit.toLabel(waitInterval.value)
            )
        }

        is ReminderRepeatRule.DuringSessionPeriod -> {
            val days = schedule.selectedDays.toShortDaysLabel()
            val interval = schedule.defaultValue.interval

            stringResource(
                R.string.repeat_detail_every,
                interval.value,
                interval.unit.toLabel(interval.value),
                days
            )
        }

        is ReminderRepeatRule.OnSchedulePeriod -> {
            val days = schedule.selectedDays.toShortDaysLabel()
            val interval = schedule.defaultValue.interval

            stringResource(
                R.string.repeat_detail_every,
                interval.value,
                interval.unit.toLabel(interval.value),
                days
            )
        }

        is ReminderRepeatRule.OnScheduleCertain -> {
            val days = schedule.selectedDays.toShortDaysLabel()
            val timesCount = schedule.defaultValue.pickedTimes.size

            when (timesCount) {
                0 -> days
                else -> pluralStringResource(
                    R.plurals.repeat_times_count,
                    timesCount,
                    timesCount,
                    days
                )
            }
        }
    }
}

@Composable
private fun Set<DayOfWeek>.toShortDaysLabel(): String {
    if (isEmpty()) return stringResource(R.string.repeat_days_never)

    if (size == DayOfWeek.entries.size) {
        return stringResource(R.string.repeat_days_every_day)
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

    if (this == weekdays) return stringResource(R.string.repeat_days_weekdays)
    if (this == weekend) return stringResource(R.string.repeat_days_weekend)

    val locale = LocalLocale.current.platformLocale

    return sortedBy { it.value }
        .joinToString(", ") { day ->
            day.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        }
}

@Composable
private fun RepeatUnit.toLabel(
    value: Int
): String {
    return when (this) {
        RepeatUnit.MINUTES -> pluralStringResource(R.plurals.repeat_unit_minutes_count, value)
        RepeatUnit.HOURS -> pluralStringResource(R.plurals.repeat_unit_hours_count, value)
        RepeatUnit.DAYS -> pluralStringResource(R.plurals.repeat_unit_days_count, value)
    }
}
