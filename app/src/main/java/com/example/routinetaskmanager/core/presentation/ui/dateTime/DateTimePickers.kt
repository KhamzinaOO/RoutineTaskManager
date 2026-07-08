package com.example.routinetaskmanager.core.presentation.ui.dateTime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker as MaterialDatePicker
import androidx.compose.material3.DatePickerDialog as MaterialDatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker as MaterialTimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.ui.CommonIconButton
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    is24Hour: Boolean = true
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title ?: stringResource(R.string.time_picker_title))
        },
        text = {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MaterialTimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text(text = stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val initialDateMillis = initialDate.toUtcStartOfDayMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        initialDisplayedMonthMillis = initialDateMillis
    )

    MaterialDatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.toUtcLocalDate()
                        ?.let(onDateSelected)
                }
            ) {
                Text(text = stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            title?.let {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    text = it,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            MaterialDatePicker(state = datePickerState)
        }
    }
}

@Composable
fun CommonDateTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dateTitle: String? = null,
    timeTitle: String? = null,
    is24Hour: Boolean = true
) {
    var step by rememberSaveable { mutableStateOf(DateTimePickerStep.Date) }
    var selectedDateEpochDay by rememberSaveable {
        mutableLongStateOf(initialDateTime.toLocalDate().toEpochDay())
    }

    when (step) {
        DateTimePickerStep.Date -> {
            CommonDatePickerDialog(
                initialDate = LocalDate.ofEpochDay(selectedDateEpochDay),
                onDateSelected = { date ->
                    selectedDateEpochDay = date.toEpochDay()
                    step = DateTimePickerStep.Time
                },
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                title = dateTitle ?: stringResource(R.string.date_picker_title)
            )
        }

        DateTimePickerStep.Time -> {
            CommonTimePickerDialog(
                initialTime = initialDateTime.toLocalTime(),
                onTimeSelected = { time ->
                    onDateTimeSelected(
                        LocalDateTime.of(
                            LocalDate.ofEpochDay(selectedDateEpochDay),
                            time
                        )
                    )
                },
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                title = timeTitle ?: stringResource(R.string.time_picker_title),
                is24Hour = is24Hour
            )
        }
    }
}

@Composable
fun TimePicker(
    startTime: String,
    endTime: String,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    isCheckboxChecked: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        if (!isCheckboxChecked) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onStartTimeClick)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = startTime,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }

                Icon(
                    Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onEndTimeClick)
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = endTime,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.time_all_day),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Checkbox(
                checked = isCheckboxChecked,
                onCheckedChange = onCheckChange,
                modifier = Modifier.scale(0.8f)
            )
        }
    }
}

@Composable
fun DateTimePicker(
    startDate: String,
    startTime: String,
    endDate: String,
    endTime: String,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    isCheckboxChecked: Boolean,
    onCheckChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isCheckboxChecked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DateTimeBlock(
                    date = startDate,
                    time = startTime,
                    onClick = onStartClick
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )

                DateTimeBlock(
                    date = endDate,
                    time = endTime,
                    onClick = onEndClick
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.time_all_day),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Checkbox(
                modifier = Modifier.scale(0.8f),
                checked = isCheckboxChecked,
                onCheckedChange = onCheckChange
            )
        }
    }
}

@Composable
private fun DateTimeBlock(
    date: String,
    time: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = time,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
fun DaysOfWeekPicker(
    modifier: Modifier,
    selectedDays : Set<DayOfWeek>,
    onDaySelected : (DayOfWeek) -> Unit
){
    val locale = LocalLocale.current.platformLocale

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(DayOfWeek.entries){ dayOfWeek ->
            val strDay = dayOfWeek.getDisplayName(
                TextStyle.NARROW_STANDALONE,
                locale
            )
            DayOfWeekBox(
                day = strDay,
                isSelected = dayOfWeek in selectedDays,
                onClick = { onDaySelected(dayOfWeek) }
            )
        }
    }
}

@Composable
fun DayOfWeekBox(
    day : String,
    isSelected : Boolean,
    onClick : () -> Unit
){
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(39.dp)
            .clickable(
                onClick = onClick
            )
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.tertiaryContainer else
                    Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false
        )
    }
}

@Composable
fun SelectedTimeBox(
    text: String,
    onClearClick: () -> Unit
) {
    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp, end = 4.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }

        CommonIconButton(
            icon = painterResource(R.drawable.ic_clear),
            contentDescription = stringResource(R.string.action_clear),
            tint = MaterialTheme.colorScheme.primary,
            onClick = onClearClick,
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.TopEnd)
        )
    }
}

private enum class DateTimePickerStep {
    Date,
    Time
}

private fun LocalDate.toUtcStartOfDayMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}

private fun Long.toUtcLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}
