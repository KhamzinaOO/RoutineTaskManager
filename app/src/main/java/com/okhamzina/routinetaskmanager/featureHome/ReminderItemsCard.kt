package com.okhamzina.routinetaskmanager.featureHome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.ui.CommonIconButton
import com.okhamzina.routinetaskmanager.core.presentation.ui.SegmentedButton
import com.okhamzina.routinetaskmanager.featureReminder.presentation.common.ui.components.ReminderCard
import com.okhamzina.routinetaskmanager.featureTask.ui.TaskCard
import com.okhamzina.routinetaskmanager.featureTask.ui.TaskCardUi

data class ReminderCardUi(
    val text : String,
    val status : Boolean,
    val time : String
)

@Composable
fun ScheduleItemsCard(
    reminders : List<ReminderCardUi>,
    tasks : List<TaskCardUi>,
    isLeftButtonPicked : Boolean,
    onLeftButtonClick: () -> Unit,
    onRightButtonClick: () -> Unit,
    onAddIconClick: () -> Unit
){
    Card(
        Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SegmentedButton(
                isLeftButtonPicked = isLeftButtonPicked,
                leftText = stringResource(R.string.tab_reminders),
                rightText = stringResource(R.string.tab_tasks),
                onLeftButtonClick = onLeftButtonClick,
                onRightButtonClick = onRightButtonClick,
                leftLeadingIcon = painterResource(R.drawable.ic_schedule),
                rightLeadingIcon = painterResource(R.drawable.ic_calendar)
            )
            if(isLeftButtonPicked){
                LazyColumn(modifier = Modifier
                    .heightIn(max = 300.dp)
                    .fillMaxWidth()
                ) {
                    if (reminders.isEmpty()){
                        item {
                            Text(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                text = stringResource(R.string.empty_reminders),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }else{
                        itemsIndexed(
                            items = reminders,
                            key = { index, reminder ->
                                "${reminder.time}-${reminder.text}-$index"
                            }
                        ) { _, reminder ->
                            ScheduleRow(
                                time = reminder.time,
                                isDone = reminder.status,
                                content = {
                                    ReminderCard(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        text = reminder.text
                                    )
                                }
                            )
                        }
                    }
                }
            }else{
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth()
                ) {
                    item {
                        Row(

                        ) {

                        }
                    }
                    if(tasks.isEmpty()){
                        item {
                            Text(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth(),
                                text = stringResource(R.string.empty_tasks),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }else{
                        itemsIndexed(
                            items = tasks,
                            key = { index, task ->
                                "${task.time}-${task.text}-$index"
                            }
                        )  { _, task ->
                            ScheduleRow(
                                time = task.time,
                                isDone = task.status,
                                content = {
                                    TaskCard(
                                        modifier = Modifier.padding(4.dp),
                                        value = task
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                CommonIconButton(
                    modifier = Modifier
                        .size(40.dp),
                    color = MaterialTheme.colorScheme.surfaceDim,
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onAddIconClick
                )
            }
        }
    }
}

@Composable
fun ScheduleRow(
    time: String,
    isDone: Boolean,
    content : @Composable (RowScope.() -> Unit)
) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )

        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier.fillMaxHeight()
            ) {
                val x = size.width / 2f
                drawLine(
                    color = lineColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (isDone) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.action_checked)
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_circle),
                        contentDescription = stringResource(R.string.state_unchecked),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        content()
    }
}
