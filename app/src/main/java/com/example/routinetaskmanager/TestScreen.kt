package com.example.routinetaskmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.core.model.DropdownMenuItem
import com.example.routinetaskmanager.core.ui.CommonButton
import com.example.routinetaskmanager.core.ui.CommonDropdownMenu
import com.example.routinetaskmanager.core.ui.CommonDropdownMenuLarge
import com.example.routinetaskmanager.core.ui.CommonTextFiled
import com.example.routinetaskmanager.core.ui.DateTimePicker
import com.example.routinetaskmanager.core.ui.DaysOfWeekPicker
import com.example.routinetaskmanager.core.ui.HandleValueChangeTextFiled
import com.example.routinetaskmanager.core.ui.InstructionsTextField
import com.example.routinetaskmanager.core.ui.NotificationSegmentedButton
import com.example.routinetaskmanager.core.ui.SegmentedButton
import com.example.routinetaskmanager.core.ui.SelectedTimeBox
import com.example.routinetaskmanager.core.ui.TimePicker
import com.example.routinetaskmanager.core.ui.WeekCarousel
import com.example.routinetaskmanager.core.ui.WorkSessionButton
import com.example.routinetaskmanager.featureTask.model.CheckboxFiledValue
import com.example.routinetaskmanager.featureTask.ui.CheckboxInstructions
import com.example.routinetaskmanager.featureTask.ui.InfoInstructionsTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ButtonTestScreen(){
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .clickable(
                onClick = { focusManager.clearFocus() }
            )
            .navigationBarsPadding()
            .systemBarsPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        CommonButton(onClick = {}, text = "button")
        SegmentedButton(
            leftText = "Default",
            rightText = "Advanced",
            onLeftButtonClick = {},
            onRightButtonClick = {}
        )
        CommonDropdownMenu(
            onDismiss = {},
            values = listOf(
                DropdownMenuItem(0, "minutes"),
                DropdownMenuItem(1, "hours"),
                DropdownMenuItem(2, "days")
            )
        )

        CommonDropdownMenuLarge(
            onDismiss = {},
            values = listOf(
                DropdownMenuItem(0, "On schedule (period)"),
                DropdownMenuItem(1, "On schedule (certain time)"),
                DropdownMenuItem(2, "During session"),
                DropdownMenuItem(3, "After another activity")
            ),
            icon = Icons.Default.Search,
            contentDescription = "Search"
        )

        CommonDropdownMenuLarge(
            onDismiss = {},
            values = listOf(
                DropdownMenuItem(0, "Never"),
                DropdownMenuItem(1, "Every N days"),
                DropdownMenuItem(2, "Every N weeks"),
                DropdownMenuItem(3, "Certain days of week")
            )
        )

        NotificationSegmentedButton {  }

        var isRunning by remember { mutableStateOf(false) }
        var elapsedTimeMillis by remember { mutableLongStateOf(0L) }

        LaunchedEffect(isRunning) {
            while (isRunning) {
                delay(1000L)
                elapsedTimeMillis += 1000L
            }
        }

        val timerText = formatTime(elapsedTimeMillis)


        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ){
            WorkSessionButton(
                0,
                timer = timerText,
                onEndClick = {isRunning = false},
                onStartClick = {isRunning = true}
            )
        }

        var textFieldValue by remember {mutableStateOf("")}
        CommonTextFiled(
            placeholder = "Enter a reminder name",
            value = textFieldValue,
            onValueChange = {textFieldValue = it}
        )

        var values by remember  { mutableStateOf(listOf(
            CheckboxFiledValue(
                id = 0,
                text = "",
                isChecked = false
            )
        ))}

        var textFieldValue2 by remember {mutableStateOf("")}

        InstructionsTextField(
            placeholder = "Instructions",
            value = textFieldValue2,
            onValueChange = { textFieldValue2 = it }
        )

        var isCheckboxEnabled by remember {mutableStateOf(false)}

        InfoInstructionsTextField(
            placeholder = "Instructions",
            value = textFieldValue2,
            onValueChange = { textFieldValue2 = it },
            values = values,
            isCheckboxEnabled = isCheckboxEnabled,
            onCheckboxValueChange = { newValue ->
                values = values.map { item ->
                    if (item.id == newValue.id) item.copy(text = newValue.text) else item
                }
            },
            onAddCheckboxClick = {
                val nextId = (values.maxOfOrNull { it.id } ?: -1) + 1
                values = values + CheckboxFiledValue(
                    id = nextId,
                    text = "",
                    isChecked = false
                )
            },
            onCheckboxIconClick = {
                isCheckboxEnabled = true
            },
            onCheckChange = {},
            onDeleteClick = { deletingItem ->
                values = values.filter { it != deletingItem }
            }
        )

        var startTime by remember { mutableStateOf("9:00") }

        var endTime by remember { mutableStateOf("19:00") }

        var  allDayEnabled by remember {mutableStateOf(false)}

        TimePicker(
            startTime = startTime,
            endTime = endTime,
            onStartTimeClick = {

            },
            onEndTimeClick = {

            },
            allDayEnabled,
            {
                allDayEnabled = !allDayEnabled
            }
        )

        var selectedDays by remember {mutableStateOf<List<DayOfWeek>>(emptyList())}
        DaysOfWeekPicker(
            selectedDays,
            { selectedDay ->
                if (selectedDay in selectedDays) selectedDays = selectedDays.filter { it != selectedDay }
                else{
                    selectedDays += selectedDay
                }
            }
        )

        DateTimePicker(
            startDate = "16 March",
            startTime = "9:00",
            endDate = "16 March",
            endTime = "19:00",
            onStartClick = {},
            onEndClick = {},
            isCheckboxChecked = false,
            onCheckChange = {}
        )

        var number by remember {mutableStateOf("0")}

        HandleValueChangeTextFiled(
            value = number,
            onValueChange = {
                number = it
            },
            onIncrement = {
                number = "${(number.toIntOrNull() ?: 0) + 1}"
            },
            onDecrement = {
                number = "${(number.toIntOrNull() ?: 0) - 1}"
            }
        )

        SelectedTimeBox(
           text = "9:00",
            {}
        )
    }
}

private fun formatTime(timeMillis: Long): String {
    val minutes = (timeMillis / 1000) / 60
    val seconds = (timeMillis / 1000) % 60

    return String.format(locale = Locale.getDefault(), "%02d:%02d", minutes, seconds)
}