package com.example.routinetaskmanager.featureReminder.presentation.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R

@Composable
fun WorkSessionButton(
    modifier: Modifier = Modifier,
    remindersCount: Int,
    timer: String,
    isActive: Boolean,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    val actionLabel = if (isActive) {
        stringResource(R.string.work_session_stop)
    } else {
        stringResource(R.string.work_session_start)
    }

    WorkSessionButtonContainer(
        modifier = modifier,
        topText = if (isActive) timer else stringResource(R.string.work_session_start),
        topTextStyle = if (isActive) {
            MaterialTheme.typography.titleLarge
        } else {
            MaterialTheme.typography.bodyMedium
        },
        bottomText = if (isActive) {
            stringResource(R.string.work_session_active_label)
        } else {
            pluralStringResource(
                R.plurals.work_session_reminders_count,
                remindersCount,
                remindersCount
            )
        },
        icon = if (isActive) painterResource(R.drawable.ic_pause) else painterResource(R.drawable.ic_play_arrow),
        actionLabel = actionLabel,
        isLoading = isLoading,
        enabled = enabled && !isLoading,
        colors = if (isActive) {
            CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        onClick = {
            if (isActive) onEndClick() else onStartClick()
        }
    )
}

@Composable
fun WorkSessionButtonContainer(
    modifier: Modifier = Modifier,
    topText: String,
    topTextStyle: TextStyle,
    bottomText: String,
    icon: Painter,
    actionLabel: String,
    isLoading: Boolean,
    enabled: Boolean,
    colors: CardColors,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(
        topEnd = 0.dp,
        topStart = 100.dp,
        bottomStart = 100.dp,
        bottomEnd = 0.dp
    )

    Card(
        modifier = modifier
            .widthIn(max = 320.dp)
            .clip(shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = actionLabel,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "$topText, $bottomText"
            },
        colors = colors,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 58.dp)
                .padding(
                top = 8.dp,
                start = 27.dp,
                end = 8.dp,
                bottom = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
        ) {
            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = topText,
                    style = topTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = bottomText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.contentColor)
                    .size(39.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.containerColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = colors.containerColor
                    )
                }
            }
        }
    }
}
