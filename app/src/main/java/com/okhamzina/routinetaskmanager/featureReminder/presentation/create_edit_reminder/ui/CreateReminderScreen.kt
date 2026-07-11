package com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.model.DropdownMenuItemUi
import com.okhamzina.routinetaskmanager.core.presentation.ui.CommonDropdownMenuLarge
import com.okhamzina.routinetaskmanager.core.presentation.ui.CommonTextFiled
import com.okhamzina.routinetaskmanager.core.presentation.ui.NotificationSegmentedButton
import com.okhamzina.routinetaskmanager.core.presentation.ui.TitleText
import com.okhamzina.routinetaskmanager.core.presentation.ui.dateTime.CommonTimePickerDialog
import com.okhamzina.routinetaskmanager.core.presentation.ui.image.FullscreenImagePagerDialog
import com.okhamzina.routinetaskmanager.core.presentation.ui.image.ImagesRowWithClearIcons
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainDayUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnScheduleCertainRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.OnSchedulePeriodRepeatUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.model.TimeWindowUi
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.ui.components.InstructionsTextField
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderMode
import com.okhamzina.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.ReminderImageUi
import com.okhamzina.routinetaskmanager.navigation.ui.AppChrome
import com.okhamzina.routinetaskmanager.navigation.ui.AppChromeEffect
import com.okhamzina.routinetaskmanager.navigation.ui.CommonTopAppBarWithArrowBack
import com.okhamzina.routinetaskmanager.navigation.ui.CreateReminder
import com.okhamzina.routinetaskmanager.navigation.ui.EditReminder
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val UiTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun CreateReminderScreen(
    uiState : CreateEditReminderUiState,
    onIntent : (CreateEditReminderIntent) -> Unit
) {

    val repeatType = uiState.repeatType

    val onSchedulePeriodState = uiState.onSchedulePeriodState

    val onScheduleCertainState = uiState.onScheduleCertainState

    val duringSessionState = uiState.duringSessionState

    val focusManager = LocalFocusManager.current

    var selectedImageIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var timePickerTarget by remember {
        mutableStateOf<TimePickerTarget?>(null)
    }

    var certainTimePickerTarget by remember {
        mutableStateOf<CertainTimePickerTarget?>(null)
    }

    AppChromeEffect(
        owner = when (uiState.screenMode){
            is CreateEditReminderMode.Create -> CreateReminder
            is CreateEditReminderMode.Edit -> EditReminder(uiState.id ?: 0)
        },
        chrome = AppChrome(
            topBar = {
                CommonTopAppBarWithArrowBack(
                    title = when (uiState.screenMode) {
                        is CreateEditReminderMode.Create -> stringResource(R.string.reminder_create_title)
                        is CreateEditReminderMode.Edit -> stringResource(R.string.reminder_edit_title)
                    },
                    onBackClick = { onIntent(CreateEditReminderIntent.BackClicked) }
                )
            }
        )
    )

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TitleText(
            text = stringResource(R.string.field_name)
        )
        CommonTextFiled(
            placeholder = stringResource(R.string.field_reminder_name_placeholder),
            value = uiState.name,
            onValueChange = { value ->
                onIntent(CreateEditReminderIntent.NameChanged(value))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        TitleText(
            text = stringResource(R.string.field_instructions)
        )
        InstructionsTextField(
            placeholder = stringResource(R.string.field_instructions),
            value = uiState.instructions,
            onValueChange = { value ->
                onIntent(CreateEditReminderIntent.InstructionsChanged(value))
            },
            onTakePictureClick = {
                onIntent(CreateEditReminderIntent.TakePictureClicked)
            }
        )

        ImagesRowWithClearIcons(
            imagePaths = uiState.images.map {
                when (it){
                    is ReminderImageUi.Picked -> it.uriString
                    is ReminderImageUi.Saved -> it.path
                }
            },
            onImageClick = { index ->
                selectedImageIndex = index
            },
            onDeleteClick = { index ->
                onIntent(CreateEditReminderIntent.ImageRemoved(uiState.images.map {
                    it.key
                }[index]))
            }
        )

        selectedImageIndex?.let { path ->
            FullscreenImagePagerDialog(
                imagePaths = uiState.images.map {
                    when (it) {
                        is ReminderImageUi.Picked -> it.uriString
                        is ReminderImageUi.Saved -> it.path
                    }
                },
                initialIndex = path,
                onDismiss = { selectedImageIndex = null }
            )
        }

        TitleText(
            text = stringResource(R.string.field_repeat_type)
        )
        CommonDropdownMenuLarge(
            selectedId = uiState.repeatType.ordinal,
            onItemClick = { item ->
                val selected = ReminderRepeatType.entries.toTypedArray().getOrElse(item.id) {
                    ReminderRepeatType.ON_SCHEDULE_PERIOD
                }
                onIntent(CreateEditReminderIntent.RepeatTypeChanged(selected))
            },
            values = repeatTypeDropdownValues(),
            icon = Icons.Default.Search,
            contentDescription = stringResource(R.string.action_search)
        )
        TitleText(
            text = stringResource(R.string.field_repeat_time)
        )

        when (repeatType) {
            ReminderRepeatType.ON_SCHEDULE_PERIOD -> {
                OnSchedulePeriodRepeatCard(
                    state = onSchedulePeriodState,
                    onStateChange = {
                        onIntent(
                            CreateEditReminderIntent.OnSchedulePeriodStateChanged(
                                it
                            )
                        )
                    },
                    dropdownValues = repeatUnitDropdownValues(),
                    onStartClick = { day ->
                        timePickerTarget = TimePickerTarget(
                            day = day,
                            field = TimeWindowField.Start
                        )
                    },
                    onEndClick = { day ->
                        timePickerTarget = TimePickerTarget(
                            day = day,
                            field = TimeWindowField.End
                        )
                    }
                )
            }

            ReminderRepeatType.ON_SCHEDULE_CERTAIN -> {
                OnScheduleCertainRepeatCard(
                    state = onScheduleCertainState,
                    onStateChange = {
                        onIntent(
                            CreateEditReminderIntent.OnScheduleCertainStateChanged(
                                it
                            )
                        )
                    },
                    onTimeClick = { day ->
                        certainTimePickerTarget = CertainTimePickerTarget(day = day)
                    }
                )
            }

            ReminderRepeatType.DURING_SESSION_PERIOD -> {
                DuringSessionPeriodRepeatCard(
                    state = duringSessionState,
                    onStateChange = {
                        onIntent(
                            CreateEditReminderIntent.DuringSessionStateChanged(
                                it
                            )
                        )
                    },
                    dropdownValues = repeatUnitDropdownValues()
                )
            }
        }

        TitleText(
            text = stringResource(R.string.field_notification_sound)
        )

        NotificationSegmentedButton(
            selectedMode = uiState.notificationMode,
            onButtonClick = { mode ->
                onIntent(CreateEditReminderIntent.NotificationModeChanged(mode))
            }
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                onClick = {
                    onIntent(CreateEditReminderIntent.SaveClicked)
                }
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        }
    }

    timePickerTarget?.let { target ->
        CommonTimePickerDialog(
            initialTime = onSchedulePeriodState.timeWindowFor(target.day)
                .timeFor(target.field)
                .toLocalTimeOrDefault(),
            onTimeSelected = { time ->
                onIntent(
                    CreateEditReminderIntent.OnSchedulePeriodStateChanged(
                        onSchedulePeriodState.updateTimeWindow(target.day) { timeWindow ->
                            timeWindow.withTime(target.field, time.format(UiTimeFormatter))
                        }
                    )
                )
                timePickerTarget = null
            },
            onDismissRequest = {
                timePickerTarget = null
            }
        )
    }

    certainTimePickerTarget?.let { target ->
        CommonTimePickerDialog(
            initialTime = onScheduleCertainState.certainTimeFor(target.day)
                .toLocalTimeOrDefault(),
            onTimeSelected = { time ->
                onIntent(
                    CreateEditReminderIntent.OnScheduleCertainStateChanged(
                        onScheduleCertainState.updateCertainTime(
                            day = target.day,
                            time = time
                        )
                    )
                )
                certainTimePickerTarget = null
            },
            onDismissRequest = {
                certainTimePickerTarget = null
            }
        )
    }
}

@Composable
private fun repeatTypeDropdownValues(): List<DropdownMenuItemUi> {
    return listOf(
        DropdownMenuItemUi(ReminderRepeatType.ON_SCHEDULE_PERIOD.ordinal, stringResource(R.string.repeat_type_on_schedule_period)),
        DropdownMenuItemUi(ReminderRepeatType.ON_SCHEDULE_CERTAIN.ordinal, stringResource(R.string.repeat_type_on_schedule_certain)),
        DropdownMenuItemUi(ReminderRepeatType.DURING_SESSION_PERIOD.ordinal, stringResource(R.string.repeat_type_during_session))
    )
}

@Composable
private fun repeatUnitDropdownValues(): List<DropdownMenuItemUi> {
    return listOf(
        DropdownMenuItemUi(RepeatUnit.MINUTES.ordinal, stringResource(R.string.repeat_unit_minutes)),
        DropdownMenuItemUi(RepeatUnit.HOURS.ordinal, stringResource(R.string.repeat_unit_hours)),
        DropdownMenuItemUi(RepeatUnit.DAYS.ordinal, stringResource(R.string.repeat_unit_days))
    )
}

private data class TimePickerTarget(
    val day: DayOfWeek?,
    val field: TimeWindowField
)

private data class CertainTimePickerTarget(
    val day: DayOfWeek?
)

private enum class TimeWindowField {
    Start,
    End
}

private fun OnSchedulePeriodRepeatUi.timeWindowFor(day: DayOfWeek?): TimeWindowUi {
    return if (day == null) {
        schedule.defaultValue.timeWindow
    } else {
        schedule.advancedEntries
            .firstOrNull { it.day == day }
            ?.value
            ?.timeWindow
            ?: schedule.defaultValue.timeWindow
    }
}

private fun OnSchedulePeriodRepeatUi.updateTimeWindow(
    day: DayOfWeek?,
    transform: (TimeWindowUi) -> TimeWindowUi
): OnSchedulePeriodRepeatUi {
    return if (day == null) {
        copy(
            schedule = schedule.copy(
                defaultValue = schedule.defaultValue.copy(
                    timeWindow = transform(schedule.defaultValue.timeWindow)
                )
            )
        )
    } else {
        copy(
            schedule = schedule.copy(
                advancedEntries = schedule.advancedEntries.map { entry ->
                    if (entry.day == day) {
                        entry.copy(
                            value = entry.value.copy(
                                timeWindow = transform(entry.value.timeWindow)
                            )
                        )
                    } else {
                        entry
                    }
                }
            )
        )
    }
}

private fun TimeWindowUi.timeFor(field: TimeWindowField): String {
    return when (field) {
        TimeWindowField.Start -> startTime
        TimeWindowField.End -> endTime
    }
}

private fun TimeWindowUi.withTime(
    field: TimeWindowField,
    time: String
): TimeWindowUi {
    return when (field) {
        TimeWindowField.Start -> copy(startTime = time)
        TimeWindowField.End -> copy(endTime = time)
    }
}

private fun OnScheduleCertainRepeatUi.certainTimeFor(day: DayOfWeek?): OnScheduleCertainDayUi {
    return if (day == null) {
        schedule.defaultValue
    } else {
        schedule.advancedEntries
            .firstOrNull { it.day == day }
            ?.value
            ?: schedule.defaultValue
    }
}

private fun OnScheduleCertainRepeatUi.updateCertainTime(
    day: DayOfWeek?,
    time: LocalTime
): OnScheduleCertainRepeatUi {
    return if (day == null) {
        copy(
            schedule = schedule.copy(
                defaultValue = schedule.defaultValue.withTime(time)
            )
        )
    } else {
        copy(
            schedule = schedule.copy(
                advancedEntries = schedule.advancedEntries.map { entry ->
                    if (entry.day == day) {
                        entry.copy(value = entry.value.withTime(time))
                    } else {
                        entry
                    }
                }
            )
        )
    }
}

private fun OnScheduleCertainDayUi.withTime(time: LocalTime): OnScheduleCertainDayUi {
    return copy(
        hours = time.format(DateTimeFormatter.ofPattern("HH")),
        minutes = time.format(DateTimeFormatter.ofPattern("mm"))
    )
}

private fun OnScheduleCertainDayUi.toLocalTimeOrDefault(): LocalTime {
    return "$hours:$minutes".toLocalTimeOrDefault()
}

private fun String.toLocalTimeOrDefault(): LocalTime {
    return runCatching {
        LocalTime.parse(this, UiTimeFormatter)
    }.getOrDefault(LocalTime.of(9, 0))
}
