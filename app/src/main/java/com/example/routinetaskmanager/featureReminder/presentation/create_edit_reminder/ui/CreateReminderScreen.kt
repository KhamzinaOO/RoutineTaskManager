package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.ui

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
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.model.DropdownMenuItemUi
import com.example.routinetaskmanager.core.presentation.ui.CommonDropdownMenuLarge
import com.example.routinetaskmanager.core.presentation.ui.CommonTextFiled
import com.example.routinetaskmanager.core.presentation.ui.NotificationSegmentedButton
import com.example.routinetaskmanager.core.presentation.ui.TitleText
import com.example.routinetaskmanager.core.presentation.ui.image.FullscreenImagePagerDialog
import com.example.routinetaskmanager.core.presentation.ui.image.ImagesRowWithClearIcons
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.InstructionsTextField
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderMode
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.ReminderImageUi
import com.example.routinetaskmanager.navigation.ui.AppChrome
import com.example.routinetaskmanager.navigation.ui.AppChromeEffect
import com.example.routinetaskmanager.navigation.ui.CommonTopAppBarWithArrowBack
import com.example.routinetaskmanager.navigation.ui.CreateReminder
import com.example.routinetaskmanager.navigation.ui.EditReminder

@Composable
fun CreateReminderScreen(
    uiState : CreateEditReminderUiState,
    onIntent : (CreateEditReminderIntent) -> Unit
) {

    val repeatType = uiState.repeatType

    val afterAnotherState = uiState.afterAnotherState

    val onSchedulePeriodState = uiState.onSchedulePeriodState

    val onScheduleCertainState = uiState.onScheduleCertainState

    val duringSessionState = uiState.duringSessionState

    val focusManager = LocalFocusManager.current

    var selectedImageIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
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
                    onStartClick = {},
                    onEndClick = {}
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

            ReminderRepeatType.AFTER_ANOTHER_ACTIVITY -> {
                AfterAnotherRepeatCard(
                    state = afterAnotherState,
                    onStateChange = {
                        onIntent(
                            CreateEditReminderIntent.AfterAnotherStateChanged(
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
}

@Composable
private fun repeatTypeDropdownValues(): List<DropdownMenuItemUi> {
    return listOf(
        DropdownMenuItemUi(ReminderRepeatType.ON_SCHEDULE_PERIOD.ordinal, stringResource(R.string.repeat_type_on_schedule_period)),
        DropdownMenuItemUi(ReminderRepeatType.ON_SCHEDULE_CERTAIN.ordinal, stringResource(R.string.repeat_type_on_schedule_certain)),
        DropdownMenuItemUi(ReminderRepeatType.DURING_SESSION_PERIOD.ordinal, stringResource(R.string.repeat_type_during_session)),
        DropdownMenuItemUi(ReminderRepeatType.AFTER_ANOTHER_ACTIVITY.ordinal, stringResource(R.string.repeat_type_after_another))
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
