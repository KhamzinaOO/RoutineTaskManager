package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.model.DropdownMenuItemUi
import com.example.routinetaskmanager.core.presentation.ui.CommonButton
import com.example.routinetaskmanager.core.presentation.ui.CommonDropdownMenu
import com.example.routinetaskmanager.core.presentation.ui.dateTime.DaysOfWeekPicker
import com.example.routinetaskmanager.core.presentation.ui.HandleValueChangeTextFiled
import com.example.routinetaskmanager.core.presentation.ui.HandleValueChangeTimeTextFiled
import com.example.routinetaskmanager.core.presentation.ui.SegmentedButton
import com.example.routinetaskmanager.core.presentation.ui.dateTime.SelectedTimeBox
import com.example.routinetaskmanager.core.presentation.ui.dateTime.TimePicker
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.presentation.common.model.AfterAnotherRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DayRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.IntervalRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodDayUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import com.example.routinetaskmanager.featureReminder.presentation.common.model.defaultWeeklyRepeatUi
import com.example.routinetaskmanager.ui.theme.RoutineTaskManagerTheme
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun RepeatTimeCardBase(
    modifier: Modifier = Modifier,
    isDefaultButtonPicked: Boolean,
    onDefaultButtonClick: () -> Unit,
    onAdvancedButtonClick: () -> Unit,
    selectedDays: Set<DayOfWeek>,
    onDaysSelected: (DayOfWeek) -> Unit,
    content: @Composable ColumnScope.(modifier: Modifier) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SegmentedButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                leftText = stringResource(R.string.repeat_mode_default),
                rightText = stringResource(R.string.repeat_mode_advanced),
                isLeftButtonPicked = isDefaultButtonPicked,
                onLeftButtonClick = onDefaultButtonClick,
                onRightButtonClick = onAdvancedButtonClick
            )

            content(Modifier.padding(horizontal = 16.dp))

            if (isDefaultButtonPicked) {
                HorizontalDivider()

                DaysOfWeekPicker(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    selectedDays = selectedDays,
                    onDaySelected = onDaysSelected
                )
            }
        }
    }
}

@Composable
fun AfterAnotherRepeatCard(
    modifier: Modifier = Modifier,
    state: AfterAnotherRepeatUi,
    onStateChange: (AfterAnotherRepeatUi) -> Unit,
    dropdownValues: List<DropdownMenuItemUi>
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        IntervalRow(
            modifier = Modifier.padding(8.dp),
            label = stringResource(R.string.repeat_wait_time),
            interval = state.waitInterval,
            onIntervalChange = { onStateChange(state.copy(waitInterval = it)) },
            dropdownValues = dropdownValues
        )
    }
}

@Composable
fun DuringSessionPeriodRepeatCard(
    modifier: Modifier = Modifier,
    state: DuringSessionPeriodRepeatUi,
    onStateChange: (DuringSessionPeriodRepeatUi) -> Unit,
    dropdownValues: List<DropdownMenuItemUi>
) {
    val schedule = state.schedule

    RepeatTimeCardBase(
        modifier = modifier,
        isDefaultButtonPicked = schedule.isDefaultMode(),
        onDefaultButtonClick = {
            onStateChange(state.copy(schedule = schedule.withMode(RepeatScheduleMode.DEFAULT)))
        },
        onAdvancedButtonClick = {
            onStateChange(state.copy(schedule = schedule.withMode(RepeatScheduleMode.ADVANCED)))
        },
        selectedDays = schedule.selectedDays,
        onDaysSelected = { day ->
            onStateChange(state.copy(schedule = schedule.toggleSelectedDay(day)))
        }
    ) { parentModifier ->
        Column(
            modifier = parentModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (schedule.isDefaultMode()) {
                DuringSessionContent(
                    value = schedule.defaultValue,
                    onValueChange = { value ->
                        onStateChange(
                            state.copy(schedule = schedule.updateDefaultValue(value))
                        )
                    },
                    dropdownValues = dropdownValues
                )
            } else {
                AdvancedCard(
                    entries = schedule.advancedEntries,
                    onCheckChange = { entry ->
                        onStateChange(
                            state.copy(schedule = schedule.updateAdvancedEntry(entry))
                        )
                    }
                ) { entry ->
                    DuringSessionContent(
                        value = entry.value,
                        onValueChange = { value ->
                            onStateChange(
                                state.copy(
                                    schedule = schedule.updateAdvancedEntry(
                                        entry.copy(value = value)
                                    )
                                )
                            )
                        },
                        dropdownValues = dropdownValues
                    )
                }
            }
        }
    }
}

@Composable
fun OnSchedulePeriodRepeatCard(
    modifier: Modifier = Modifier,
    state: OnSchedulePeriodRepeatUi,
    onStateChange: (OnSchedulePeriodRepeatUi) -> Unit,
    dropdownValues: List<DropdownMenuItemUi>,
    onStartClick: (DayOfWeek?) -> Unit,
    onEndClick: (DayOfWeek?) -> Unit
) {
    val schedule = state.schedule

    RepeatTimeCardBase(
        modifier = modifier,
        isDefaultButtonPicked = schedule.isDefaultMode(),
        onDefaultButtonClick = {
            onStateChange(state.copy(schedule = schedule.withMode(RepeatScheduleMode.DEFAULT)))
        },
        onAdvancedButtonClick = {
            onStateChange(state.copy(schedule = schedule.withMode(RepeatScheduleMode.ADVANCED)))
        },
        selectedDays = schedule.selectedDays,
        onDaysSelected = { day ->
            onStateChange(state.copy(schedule = schedule.toggleSelectedDay(day)))
        }
    ) { parentModifier ->
        Column(
            modifier = parentModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (schedule.isDefaultMode()) {
                OnSchedulePeriodContent(
                    value = schedule.defaultValue,
                    onValueChange = { value ->
                        onStateChange(
                            state.copy(schedule = schedule.updateDefaultValue(value))
                        )
                    },
                    dropdownValues = dropdownValues,
                    onStartClick = { onStartClick(null) },
                    onEndClick = { onEndClick(null) }
                )
            } else {
                AdvancedCard(
                    entries = schedule.advancedEntries,
                    onCheckChange = { entry ->
                        onStateChange(
                            state.copy(schedule = schedule.updateAdvancedEntry(entry))
                        )
                    }
                ) { entry ->
                    OnSchedulePeriodContent(
                        value = entry.value,
                        onValueChange = { value ->
                            onStateChange(
                                state.copy(
                                    schedule = schedule.updateAdvancedEntry(
                                        entry.copy(value = value)
                                    )
                                )
                            )
                        },
                        dropdownValues = dropdownValues,
                        onStartClick = { onStartClick(entry.day) },
                        onEndClick = { onEndClick(entry.day) }
                    )
                }
            }
        }
    }
}

@Composable
fun OnScheduleCertainRepeatCard(
    modifier: Modifier = Modifier,
    state: OnScheduleCertainRepeatUi,
    onStateChange: (OnScheduleCertainRepeatUi) -> Unit
) {
    val schedule = state.schedule

    RepeatTimeCardBase(
        modifier = modifier,
        isDefaultButtonPicked = schedule.isDefaultMode(),
        onDefaultButtonClick = {
            onStateChange(state.copy(schedule = schedule.withMode(RepeatScheduleMode.DEFAULT)))
        },
        onAdvancedButtonClick = {
            onStateChange(state.copy(schedule = schedule.withMode(RepeatScheduleMode.ADVANCED)))
        },
        selectedDays = schedule.selectedDays,
        onDaysSelected = { day ->
            onStateChange(state.copy(schedule = schedule.toggleSelectedDay(day)))
        }
    ) { parentModifier ->
        Column(
            modifier = parentModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (schedule.isDefaultMode()) {
                OnScheduleCertainContent(
                    value = schedule.defaultValue,
                    onValueChange = { value ->
                        onStateChange(
                            state.copy(schedule = schedule.updateDefaultValue(value))
                        )
                    }
                )
            } else {
                AdvancedCard(
                    entries = schedule.advancedEntries,
                    onCheckChange = { entry ->
                        onStateChange(
                            state.copy(schedule = schedule.updateAdvancedEntry(entry))
                        )
                    }
                ) { entry ->
                    OnScheduleCertainContent(
                        value = entry.value,
                        onValueChange = { value ->
                            onStateChange(
                                state.copy(
                                    schedule = schedule.updateAdvancedEntry(
                                        entry.copy(value = value)
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun <T> AdvancedCard(
    entries: List<DayRepeatUi<T>>,
    onCheckChange: (DayRepeatUi<T>) -> Unit,
    content: @Composable ColumnScope.(DayRepeatUi<T>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEach { item ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = item.day.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Switch(
                        checked = item.enabled,
                        onCheckedChange = { value ->
                            onCheckChange(item.copy(enabled = value))
                        }
                    )
                }

                if (item.enabled) {
                    content(item)
                }
            }
        }
    }
}

@Composable
fun DuringSessionContent(
    value: IntervalRepeatUi,
    onValueChange: (IntervalRepeatUi) -> Unit,
    dropdownValues: List<DropdownMenuItemUi>
) {
    IntervalRow(
        label = stringResource(R.string.repeat_every),
        interval = value.interval,
        onIntervalChange = { onValueChange(value.copy(interval = it)) },
        dropdownValues = dropdownValues
    )
}

@Composable
fun OnSchedulePeriodContent(
    value: OnSchedulePeriodDayUi,
    onValueChange: (OnSchedulePeriodDayUi) -> Unit,
    dropdownValues: List<DropdownMenuItemUi>,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IntervalRow(
            label = stringResource(R.string.repeat_every),
            interval = value.interval,
            onIntervalChange = { interval ->
                onValueChange(value.copy(interval = interval))
            },
            dropdownValues = dropdownValues
        )

        TimePicker(
            startTime = value.timeWindow.startTime,
            endTime = value.timeWindow.endTime,
            onStartTimeClick = onStartClick,
            onEndTimeClick = onEndClick,
            isCheckboxChecked = value.timeWindow.allDayEnabled,
            onCheckChange = { checked ->
                onValueChange(
                    value.copy(
                        timeWindow = value.timeWindow.copy(allDayEnabled = checked)
                    )
                )
            }
        )
    }
}

@Composable
fun OnScheduleCertainContent(
    value: OnScheduleCertainDayUi,
    onValueChange: (OnScheduleCertainDayUi) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationTimeRow(
            value = value,
            onValueChange = onValueChange
        )

        PickedTimeRow(
            pickedTimes = value.pickedTimes,
            onTimeDeleteClick = { time ->
                onValueChange(value.copy(pickedTimes = value.pickedTimes - time))
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntervalRow(
    modifier: Modifier = Modifier,
    label: String,
    interval: RepeatIntervalUi,
    onIntervalChange: (RepeatIntervalUi) -> Unit,
    dropdownValues: List<DropdownMenuItemUi>
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        HandleValueChangeTextFiled(
            value = interval.value,
            onValueChange = { value ->
                onIntervalChange(interval.copy(value = value))
            },
            onIncrement = {
                onIntervalChange(interval.increment())
            },
            onDecrement = {
                onIntervalChange(interval.decrement())
            }
        )

        CommonDropdownMenu(
            selectedId = interval.selectedUnitId,
            onItemClick = { unit ->
                onIntervalChange(interval.copy(selectedUnitId = unit.id))
            },
            values = dropdownValues
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotificationTimeRow(
    value: OnScheduleCertainDayUi,
    onValueChange: (OnScheduleCertainDayUi) -> Unit
) {
    val focusManager = LocalFocusManager.current

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.repeat_notification_time),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        HandleValueChangeTimeTextFiled(
            hours = value.hours,
            minutes = value.minutes,
            onHoursChange = { hours ->
                onValueChange(value.copy(hours = hours))
            },
            onMinutesChange = { minutes ->
                onValueChange(value.copy(minutes = minutes))
            },
            hoursKeyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            minutesKeyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            onIncrement = {
                onValueChange(value.shiftInputTime(minutesDelta = 1))
            },
            onDecrement = {
                onValueChange(value.shiftInputTime(minutesDelta = -1))
            }
        )

        CommonButton(
            onClick = {
                onValueChange(value.addInputTime())
            },
            text = stringResource(R.string.action_add)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PickedTimeRow(
    pickedTimes: Set<LocalTime>,
    onTimeDeleteClick: (LocalTime) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        pickedTimes.sorted().forEach { time ->
            SelectedTimeBox(
                text = time.format(TimeFormatter),
                onClearClick = { onTimeDeleteClick(time) }
            )
        }
    }
}

private fun RepeatIntervalUi.increment(): RepeatIntervalUi {
    return copy(value = ((value.toIntOrNull() ?: 0) + 1).toString())
}

private fun RepeatIntervalUi.decrement(): RepeatIntervalUi {
    val current = value.toIntOrNull() ?: 1
    return copy(value = (current - 1).coerceAtLeast(1).toString())
}

private fun OnScheduleCertainDayUi.addInputTime(): OnScheduleCertainDayUi {
    return copy(pickedTimes = pickedTimes + inputTime())
}

private fun OnScheduleCertainDayUi.shiftInputTime(minutesDelta: Long): OnScheduleCertainDayUi {
    val shiftedTime = inputTime().plusMinutes(minutesDelta)
    return copy(
        hours = shiftedTime.format(DateTimeFormatter.ofPattern("HH")),
        minutes = shiftedTime.format(DateTimeFormatter.ofPattern("mm"))
    )
}

private fun OnScheduleCertainDayUi.inputTime(): LocalTime {
    val parsedHours = hours.toIntOrNull()?.coerceIn(0, 23) ?: 0
    val parsedMinutes = minutes.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return LocalTime.of(parsedHours, parsedMinutes)
}

private fun <T> WeeklyRepeatUi<T>.isDefaultMode(): Boolean {
    return mode == RepeatScheduleMode.DEFAULT
}

private fun <T> WeeklyRepeatUi<T>.withMode(mode: RepeatScheduleMode): WeeklyRepeatUi<T> {
    if (mode == RepeatScheduleMode.DEFAULT) {
        return copy(mode = mode)
    }

    return copy(
        mode = mode,
        advancedEntries = advancedEntries.map { entry ->
            entry.copy(enabled = entry.enabled || entry.day in selectedDays)
        }
    )
}

private fun <T> WeeklyRepeatUi<T>.toggleSelectedDay(day: DayOfWeek): WeeklyRepeatUi<T> {
    val updatedSelectedDays = if (day in selectedDays) {
        selectedDays - day
    } else {
        selectedDays + day
    }

    return copy(selectedDays = updatedSelectedDays)
}

private fun <T> WeeklyRepeatUi<T>.updateDefaultValue(value: T): WeeklyRepeatUi<T> {
    return copy(defaultValue = value)
}

private fun <T> WeeklyRepeatUi<T>.updateAdvancedEntry(
    updatedEntry: DayRepeatUi<T>
): WeeklyRepeatUi<T> {
    return copy(
        advancedEntries = advancedEntries.map { entry ->
            if (entry.day == updatedEntry.day) updatedEntry else entry
        }
    )
}

@Composable
private fun previewDropdownValues(): List<DropdownMenuItemUi> {
    return listOf(
        DropdownMenuItemUi(RepeatUnit.MINUTES.ordinal, stringResource(R.string.repeat_unit_minutes)),
        DropdownMenuItemUi(RepeatUnit.HOURS.ordinal, stringResource(R.string.repeat_unit_hours)),
        DropdownMenuItemUi(RepeatUnit.DAYS.ordinal, stringResource(R.string.repeat_unit_days))
    )
}

@Preview(name = "AfterAnotherRepeatCard")
@Composable
private fun AfterAnotherRepeatCardPreview() {
    var state by remember { mutableStateOf(AfterAnotherRepeatUi()) }

    RoutineTaskManagerTheme {
        AfterAnotherRepeatCard(
            state = state,
            onStateChange = { state = it },
            dropdownValues = previewDropdownValues()
        )
    }
}

@Preview(name = "DuringSessionPeriodRepeatCard")
@Composable
private fun DuringSessionPeriodRepeatCardPreview() {
    var state by remember {
        mutableStateOf(
            DuringSessionPeriodRepeatUi(
                schedule = defaultWeeklyRepeatUi(
                    defaultValue = IntervalRepeatUi(),
                    selectedDays = previewSelectedDays()
                )
            )
        )
    }

    RoutineTaskManagerTheme {
        DuringSessionPeriodRepeatCard(
            state = state,
            onStateChange = { state = it },
            dropdownValues = previewDropdownValues()
        )
    }
}

@Preview(name = "OnSchedulePeriodRepeatCard")
@Composable
private fun OnSchedulePeriodRepeatCardPreview() {
    var state by remember {
        mutableStateOf(
            OnSchedulePeriodRepeatUi(
                schedule = defaultWeeklyRepeatUi(
                    defaultValue = OnSchedulePeriodDayUi(),
                    selectedDays = previewSelectedDays()
                )
            )
        )
    }

    RoutineTaskManagerTheme {
        OnSchedulePeriodRepeatCard(
            state = state,
            onStateChange = { state = it },
            dropdownValues = previewDropdownValues(),
            onStartClick = {},
            onEndClick = {}
        )
    }
}

@Preview(name = "OnScheduleCertainRepeatCard")
@Composable
private fun OnScheduleCertainRepeatCardPreview() {
    var state by remember {
        mutableStateOf(
            OnScheduleCertainRepeatUi(
                schedule = defaultWeeklyRepeatUi(
                    defaultValue = OnScheduleCertainDayUi(
                        pickedTimes = setOf(
                            LocalTime.of(9, 0),
                            LocalTime.of(18, 0)
                        )
                    ),
                    selectedDays = previewSelectedDays()
                )
            )
        )
    }

    RoutineTaskManagerTheme {
        OnScheduleCertainRepeatCard(
            state = state,
            onStateChange = { state = it }
        )
    }
}

private fun previewSelectedDays(): Set<DayOfWeek> {
    return setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.FRIDAY
    )
}
