package com.example.routinetaskmanager.featureReminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.model.DropdownMenuItemUi
import com.example.routinetaskmanager.core.ui.CommonDropdownMenuLarge
import com.example.routinetaskmanager.core.ui.CommonTextFiled
import com.example.routinetaskmanager.core.ui.NotificationSegmentedButton
import com.example.routinetaskmanager.core.ui.TitleText
import com.example.routinetaskmanager.navigation.ui.AppChrome
import com.example.routinetaskmanager.navigation.ui.AppChromeEffect
import com.example.routinetaskmanager.navigation.ui.CommonTopAppBarWithArrowBack

@Composable
fun CreateReminderScreen(
    onBackClick: () -> Unit
) {
    var repeatType by remember {
        mutableStateOf(ReminderRepeatType.ON_SCHEDULE_PERIOD)
    }
    var afterAnotherState by remember {
        mutableStateOf(AfterAnotherRepeatUi())
    }
    var onSchedulePeriodState by remember {
        mutableStateOf(OnSchedulePeriodRepeatUi())
    }
    var onScheduleCertainState by remember {
        mutableStateOf(OnScheduleCertainRepeatUi())
    }
    var duringSessionState by remember {
        mutableStateOf(DuringSessionPeriodRepeatUi())
    }

    AppChromeEffect(
        chrome = AppChrome(
            topBar = {
                CommonTopAppBarWithArrowBack(
                    title = "Create new reminder",
                    onBackClick = onBackClick
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
            value = "Enter a reminder name",
            onValueChange = {}
        )
        TitleText(
            text = "Instructions"
        )
        InstructionsTextField(
            placeholder = "Instructions",
            value = "Instructions",
            onValueChange = {},
            onTakePictureClick = {}
        )
        TitleText(
            text = "Repeat type"
        )
        CommonDropdownMenuLarge(
            selectedId = repeatType.ordinal,
            onDismiss = { id ->
                repeatType = ReminderRepeatType.values().getOrElse(id) {
                    ReminderRepeatType.ON_SCHEDULE_PERIOD
                }
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
                    onStateChange = { onSchedulePeriodState = it },
                    dropdownValues = repeatUnitDropdownValues(),
                    onStartClick = {},
                    onEndClick = {}
                )
            }

            ReminderRepeatType.ON_SCHEDULE_CERTAIN -> {
                OnScheduleCertainRepeatCard(
                    state = onScheduleCertainState,
                    onStateChange = { onScheduleCertainState = it }
                )
            }

            ReminderRepeatType.DURING_SESSION_PERIOD -> {
                DuringSessionPeriodRepeatCard(
                    state = duringSessionState,
                    onStateChange = { duringSessionState = it },
                    dropdownValues = repeatUnitDropdownValues()
                )
            }

            ReminderRepeatType.AFTER_ANOTHER_ACTIVITY -> {
                AfterAnotherRepeatCard(
                    state = afterAnotherState,
                    onStateChange = { afterAnotherState = it },
                    dropdownValues = repeatUnitDropdownValues()
                )
            }
        }

        TitleText(
            text = "Notification sound"
        )
        NotificationSegmentedButton { }
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
