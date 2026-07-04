package com.example.routinetaskmanager.featureReminder.presentation.mapper

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.featureReminder.domain.model.Reminder
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatRule
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.domain.model.TimeWindow
import com.example.routinetaskmanager.featureReminder.presentation.all_reminders.model.ReminderMiniCardUi
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
fun ReminderRepeatRule.toRepeatLabel(): String {
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
fun ReminderRepeatRule.toShortDetailsLabel(): String? {
    return when (this) {
        is ReminderRepeatRule.AfterAnother -> {
            waitInterval.unit.toAfterDetailLabel(waitInterval.value)
        }

        is ReminderRepeatRule.DuringSessionPeriod -> {
            val days = schedule.selectedDays.toShortDaysLabel()
            val interval = schedule.defaultValue.interval

            interval.unit.toEveryDetailLabel(
                value = interval.value,
                days = days
            )
        }

        is ReminderRepeatRule.OnSchedulePeriod -> {
            val time = schedule.defaultValue.timeWindow
            val days = schedule.selectedDays.toShortDaysLabel()
            val interval = schedule.defaultValue.interval

            time.toTimeIntervalLabel() + "\n" +
            interval.unit.toEveryDetailLabel(
                value = interval.value,
                days = days
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
fun Set<DayOfWeek>.toShortDaysLabel(): String {
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
fun RepeatUnit.toAfterDetailLabel(value: Int): String {
    return when (this) {
        RepeatUnit.MINUTES -> pluralStringResource(R.plurals.repeat_detail_after_minutes, value, value)
        RepeatUnit.HOURS -> pluralStringResource(R.plurals.repeat_detail_after_hours, value, value)
        RepeatUnit.DAYS -> pluralStringResource(R.plurals.repeat_detail_after_days, value, value)
    }
}

@Composable
fun RepeatUnit.toEveryDetailLabel(
    value: Int,
    days: String
): String {
    return when (this) {
        RepeatUnit.MINUTES -> pluralStringResource(R.plurals.repeat_detail_every_minutes, value, value, days)
        RepeatUnit.HOURS -> pluralStringResource(R.plurals.repeat_detail_every_hours, value, value, days)
        RepeatUnit.DAYS -> pluralStringResource(R.plurals.repeat_detail_every_days, value, value, days)
    }
}

@Composable
fun TimeWindow.toTimeIntervalLabel(): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val locale = remember(configuration) {
        ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    }

    val is24HourFormat = DateFormat.is24HourFormat(context)

    val timeFormatter = remember(locale, is24HourFormat) {
        val skeleton = if (is24HourFormat) "Hm" else "hm"
        val pattern = DateFormat.getBestDateTimePattern(locale, skeleton)

        DateTimeFormatter.ofPattern(pattern, locale)
    }

    return when {
        allDayEnabled -> stringResource(R.string.time_all_day)
        else -> {
            val start = startTime.format(timeFormatter)
            val end = endTime.format(timeFormatter)

            "$start - $end"
        }
    }
}