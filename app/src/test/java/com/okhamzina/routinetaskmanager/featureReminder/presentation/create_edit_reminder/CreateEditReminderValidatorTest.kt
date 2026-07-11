package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder

import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.DayRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreateEditReminderValidatorTest {

    @Test
    fun validate_allowsAdvancedScheduleWithEnabledEntryAndEmptySelectedDays() {
        val state = validBaseState().copy(
            onScheduleCertainState = OnScheduleCertainRepeatUi(
                schedule = WeeklyRepeatUi(
                    mode = RepeatScheduleMode.ADVANCED,
                    selectedDays = emptySet(),
                    defaultValue = OnScheduleCertainDayUi(),
                    advancedEntries = listOf(
                        DayRepeatUi(
                            day = DayOfWeek.MONDAY,
                            enabled = true,
                            value = OnScheduleCertainDayUi(
                                pickedTimes = setOf(LocalTime.of(9, 0))
                            )
                        )
                    )
                )
            )
        )

        val result = CreateEditReminderValidator.validate(state)

        assertNull(result)
    }

    @Test
    fun validate_rejectsCertainScheduleWhenTypedTimeWasNotAdded() {
        val state = validBaseState().copy(
            onScheduleCertainState = OnScheduleCertainRepeatUi(
                schedule = WeeklyRepeatUi(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.MONDAY),
                    defaultValue = OnScheduleCertainDayUi(
                        hours = "09",
                        minutes = "00",
                        pickedTimes = emptySet()
                    ),
                    advancedEntries = emptyList()
                )
            )
        )

        val result = CreateEditReminderValidator.validate(state)

        assertEquals(
            UiText.StringResource(R.string.error_add_valid_time),
            result
        )
    }

    @Test
    fun validate_rejectsDefaultScheduleWithoutSelectedDays() {
        val state = validBaseState().copy(
            onScheduleCertainState = OnScheduleCertainRepeatUi(
                schedule = WeeklyRepeatUi(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = emptySet(),
                    defaultValue = OnScheduleCertainDayUi(
                        pickedTimes = setOf(LocalTime.of(9, 0))
                    ),
                    advancedEntries = emptyList()
                )
            )
        )

        val result = CreateEditReminderValidator.validate(state)

        assertEquals(
            UiText.StringResource(R.string.error_select_day),
            result
        )
    }

    private fun validBaseState(): CreateEditReminderUiState {
        return CreateEditReminderUiState(
            name = "Drink water",
            repeatType = ReminderRepeatType.ON_SCHEDULE_CERTAIN,
            onScheduleCertainState = OnScheduleCertainRepeatUi(
                schedule = WeeklyRepeatUi(
                    mode = RepeatScheduleMode.DEFAULT,
                    selectedDays = setOf(DayOfWeek.MONDAY),
                    defaultValue = OnScheduleCertainDayUi(
                        pickedTimes = setOf(LocalTime.of(9, 0))
                    ),
                    advancedEntries = emptyList()
                )
            )
        )
    }
}
