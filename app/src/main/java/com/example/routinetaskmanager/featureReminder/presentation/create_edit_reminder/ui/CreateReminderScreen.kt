package com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.presentation.model.DropdownMenuItemUi
import com.example.routinetaskmanager.core.presentation.ui.CommonDropdownMenuLarge
import com.example.routinetaskmanager.core.presentation.ui.CommonTextFiled
import com.example.routinetaskmanager.core.presentation.ui.NotificationSegmentedButton
import com.example.routinetaskmanager.core.presentation.ui.TitleText
import com.example.routinetaskmanager.core.presentation.ui.image.FullscreenImagePagerDialog
import com.example.routinetaskmanager.core.presentation.ui.image.ImagesRow
import com.example.routinetaskmanager.core.presentation.ui.image.ImagesRowWithClearIcons
import com.example.routinetaskmanager.featureReminder.domain.model.ReminderRepeatType
import com.example.routinetaskmanager.featureReminder.domain.model.RepeatUnit
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderIntent
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderUiState
import com.example.routinetaskmanager.featureReminder.presentation.common.ui.components.InstructionsTextField
import com.example.routinetaskmanager.featureReminder.presentation.create_edit_reminder.model.CreateEditReminderMode
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
                        is CreateEditReminderMode.Create -> "Create new reminder"
                        is CreateEditReminderMode.Edit -> "Edit reminder"
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
            text = "Name"
        )
        CommonTextFiled(
            placeholder = "Enter a reminder name",
            value = uiState.name,
            onValueChange = { value ->
                onIntent(CreateEditReminderIntent.NameChanged(value))
            }
        )
        TitleText(
            text = "Instructions"
        )
        InstructionsTextField(
            placeholder = "Instructions",
            value = uiState.instructions,
            onValueChange = { value ->
                onIntent(CreateEditReminderIntent.InstructionsChanged(value))
            },
            onTakePictureClick = {
                onIntent(CreateEditReminderIntent.TakePictureClicked)
            }
        )

        ImagesRowWithClearIcons(
            imagePaths = uiState.imagePaths,
            onImageClick = { index ->
                selectedImageIndex = index
            },
            onDeleteClick = { index ->
                onIntent(CreateEditReminderIntent.ImageRemoved(uiState.imagePaths[index]))
            }
        )

        selectedImageIndex?.let { path ->
            FullscreenImagePagerDialog(
                imagePaths = uiState.imagePaths,
                initialIndex = path,
                onDismiss = { selectedImageIndex = null }
            )
        }

        TitleText(
            text = "Repeat type"
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
            contentDescription = "Search"
        )
        TitleText(
            text = "Repeat time"
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
            text = "Notification sound"
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
                onClick = {
                    onIntent(CreateEditReminderIntent.SaveClicked)
                }
            ) {
                Text(
                    text = "Save"
                )
            }
        }
    }
}

private fun repeatTypeDropdownValues(): List<DropdownMenuItemUi> {
    return listOf(
        DropdownMenuItemUi(ReminderRepeatType.ON_SCHEDULE_PERIOD.ordinal, "On schedule (period)"),
        DropdownMenuItemUi(ReminderRepeatType.ON_SCHEDULE_CERTAIN.ordinal, "On schedule (certain time)"),
        DropdownMenuItemUi(ReminderRepeatType.DURING_SESSION_PERIOD.ordinal, "During session"),
        DropdownMenuItemUi(ReminderRepeatType.AFTER_ANOTHER_ACTIVITY.ordinal, "After another activity")
    )
}

private fun repeatUnitDropdownValues(): List<DropdownMenuItemUi> {
    return listOf(
        DropdownMenuItemUi(RepeatUnit.MINUTES.ordinal, "minutes"),
        DropdownMenuItemUi(RepeatUnit.HOURS.ordinal, "hours"),
        DropdownMenuItemUi(RepeatUnit.DAYS.ordinal, "days")
    )
}
