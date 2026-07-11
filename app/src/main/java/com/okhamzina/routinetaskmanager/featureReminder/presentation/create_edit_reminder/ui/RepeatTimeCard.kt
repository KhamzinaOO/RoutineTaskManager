package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.ui

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.model.DropdownMenuItemUi
import com.okhamzina.routinetaskmanager.core.presentation.ui.CommonButton
import com.okhamzina.routinetaskmanager.core.presentation.ui.CommonDropdownMenu
import com.okhamzina.routinetaskmanager.core.presentation.ui.dateTime.DaysOfWeekPicker
import com.okhamzina.routinetaskmanager.core.presentation.ui.HandleValueChangeTextFiled
import com.okhamzina.routinetaskmanager.core.presentation.ui.HandleValueChangeTimeTextFiled
import com.okhamzina.routinetaskmanager.core.presentation.ui.SegmentedButton
import com.okhamzina.routinetaskmanager.core.presentation.ui.dateTime.SelectedTimeBox
import com.okhamzina.routinetaskmanager.core.presentation.ui.dateTime.TimePicker
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatScheduleMode
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.DayRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.DuringSessionPeriodRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.IntervalRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.RepeatIntervalUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.WeeklyRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.defaultWeeklyRepeatUi
import com.okhamzina.routinetaskmanager.ui.theme.RoutineTaskManagerTheme
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
    onStateChange: (OnScheduleCertainRepeatUi) -> Unit,
    onTimeClick: ((DayOfWeek?) -> Unit)? = null
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
                    },
                    onTimeClick = onTimeClick?.let { click -> { click(null) } }
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
                        },
                        onTimeClick = onTimeClick?.let { click -> { click(entry.day) } }
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
        label = value.interval.toEveryLabel(),
        interval = value.interval,
        onIntervalChange = { interval ->
            onValueChange(value.copy(interval = interval))
        },
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
            label = value.interval.toEveryLabel(),
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
    onValueChange: (OnScheduleCertainDayUi) -> Unit,
    onTimeClick: (() -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationTimeRow(
            value = value,
            onValueChange = onValueChange,
            onTimeClick = onTimeClick
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
    val localizedDropdownValues = dropdownValues.toIntervalUnitLabels(interval.quantityForDisplay())

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
            values = localizedDropdownValues
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotificationTimeRow(
    value: OnScheduleCertainDayUi,
    onValueChange: (OnScheduleCertainDayUi) -> Unit,
    onTimeClick: (() -> Unit)?
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
            },
            onClick = onTimeClick
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

@Composable
private fun RepeatIntervalUi.toEveryLabel(): String {
    val unit = RepeatUnit.entries.getOrNull(selectedUnitId)

    return when {
        unit == RepeatUnit.MINUTES && quantityForDisplay().usesSingularOneForm() -> {
            stringResource(R.string.repeat_every_feminine)
        }
        unit != null && quantityForDisplay().usesSingularOneForm() -> {
            stringResource(R.string.repeat_every_masculine)
        }
        else -> stringResource(R.string.repeat_every)
    }
}

private fun RepeatIntervalUi.quantityForDisplay(): Int {
    return value.toIntOrNull() ?: 0
}

@Composable
private fun List<DropdownMenuItemUi>.toIntervalUnitLabels(quantity: Int): List<DropdownMenuItemUi> {
    return map { item ->
        val unit = RepeatUnit.entries.getOrNull(item.id)

        if (unit == null) {
            item
        } else {
            item.copy(name = unit.toIntervalUnitLabel(quantity))
        }
    }
}

@Composable
private fun RepeatUnit.toIntervalUnitLabel(quantity: Int): String {
    return when (this) {
        RepeatUnit.MINUTES -> pluralStringResource(R.plurals.repeat_unit_minutes_count, quantity)
        RepeatUnit.HOURS -> pluralStringResource(R.plurals.repeat_unit_hours_count, quantity)
        RepeatUnit.DAYS -> pluralStringResource(R.plurals.repeat_unit_days_count, quantity)
    }
}

private fun Int.usesSingularOneForm(): Boolean {
    val positive = kotlin.math.abs(this)

    return positive % 10 == 1 && positive % 100 != 11
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
