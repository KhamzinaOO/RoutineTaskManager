package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder

import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.model.UiText
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.TimeWindowUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import java.time.LocalTime

object CreateEditReminderValidator {

    fun validate(state: CreateEditReminderUiState): UiText? {
        if (state.name.isBlank()) {
            return UiText.StringResource(R.string.error_enter_reminder_name)
        }

        return when (state.repeatType) {
            ReminderRepeatType.ON_SCHEDULE_PERIOD -> {
                validateOnSchedulePeriod(state.onSchedulePeriodState)
            }

            ReminderRepeatType.ON_SCHEDULE_CERTAIN -> {
                validateOnScheduleCertain(state.onScheduleCertainState)
            }

            ReminderRepeatType.DURING_SESSION_PERIOD -> {
                validateDuringSession(state.duringSessionState)
            }
        }
    }

    private fun validateDuringSession(
        state: DuringSessionPeriodRepeatUi
    ): UiText? {
        return validateWeeklyRepeat(
            schedule = state.schedule,
            valueValidator = { intervalRepeat ->
                validateRepeatInterval(intervalRepeat.interval)
            }
        )
    }

    private fun validateOnSchedulePeriod(
        state: OnSchedulePeriodRepeatUi
    ): UiText? {
        return validateWeeklyRepeat(
            schedule = state.schedule,
            valueValidator = { day ->
                validateRepeatInterval(day.interval)
                    ?: validateTimeWindow(day.timeWindow)
            }
        )
    }

    private fun validateOnScheduleCertain(
        state: OnScheduleCertainRepeatUi
    ): UiText? {
        return validateWeeklyRepeat(
            schedule = state.schedule,
            valueValidator = { day ->
                validateCertainTime(day)
            }
        )
    }

    private fun validateRepeatInterval(
        interval: RepeatIntervalUi
    ): UiText? {
        val value = interval.value.trim().toIntOrNull()

        if (value == null) {
            return UiText.StringResource(R.string.error_repeat_interval_number)
        }

        if (value <= 0) {
            return UiText.StringResource(R.string.error_repeat_interval_positive)
        }

        return null
    }

    private fun validateTimeWindow(
        timeWindow: TimeWindowUi
    ): UiText? {
        if (timeWindow.allDayEnabled) {
            return null
        }

        val start = runCatching {
            LocalTime.parse(timeWindow.startTime)
        }.getOrNull()

        val end = runCatching {
            LocalTime.parse(timeWindow.endTime)
        }.getOrNull()

        if (start == null || end == null) {
            return UiText.StringResource(R.string.error_time_window_valid)
        }

        if (!start.isBefore(end)) {
            return UiText.StringResource(R.string.error_start_before_end)
        }

        return null
    }

    private fun validateCertainTime(
        day: OnScheduleCertainDayUi
    ): UiText? {
        if (day.pickedTimes.isEmpty()) {
            return UiText.StringResource(R.string.error_add_valid_time)
        }

        return null
    }

    private fun <T> validateWeeklyRepeat(
        schedule: WeeklyRepeatUi<T>,
        valueValidator: (T) -> UiText?
    ): UiText? {
        return when (schedule.mode) {
            RepeatScheduleMode.DEFAULT -> {
                if (schedule.selectedDays.isEmpty()) {
                    return UiText.StringResource(R.string.error_select_day)
                }

                valueValidator(schedule.defaultValue)
            }

            RepeatScheduleMode.ADVANCED -> {
                val enabledEntries = schedule.advancedEntries.filter { it.enabled }

                if (enabledEntries.isEmpty()) {
                    return UiText.StringResource(R.string.error_enable_day)
                }

                enabledEntries
                    .firstNotNullOfOrNull { entry ->
                        valueValidator(entry.value)
                    }
            }
        }
    }
}
