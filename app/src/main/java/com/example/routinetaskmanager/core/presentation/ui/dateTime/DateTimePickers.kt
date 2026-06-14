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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.ui.CommonIconButton
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

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
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Icon(
                    Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Arrow Forward",
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
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "All day",
                style = MaterialTheme.typography.bodyLarge
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
                    contentDescription = "Arrow Forward",
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
                text = "All day",
                style = MaterialTheme.typography.bodyLarge
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
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = time,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun DaysOfWeekPicker(
    modifier: Modifier,
    selectedDays : Set<DayOfWeek>,
    onDaySelected : (DayOfWeek) -> Unit
){
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(DayOfWeek.entries){ dayOfWeek ->
            val strDay = dayOfWeek.getDisplayName(
                TextStyle.NARROW_STANDALONE,
                Locale.getDefault()
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
            style = MaterialTheme.typography.bodyLarge
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
                color = MaterialTheme.colorScheme.secondary
            )
        }

        CommonIconButton(
            icon = painterResource(R.drawable.ic_clear),
            contentDescription = "clear",
            tint = MaterialTheme.colorScheme.primary,
            onClick = onClearClick,
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.TopEnd)
        )
    }
}