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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R

@Composable
fun WorkSessionButton(
    remindersCount: Int,
    timer: String,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    var isActive by remember { mutableStateOf(false) }

    WorkSessionButtonContainer(
        topText = if (isActive) timer else "Start work session",
        topTextStyle = if (isActive) {
            MaterialTheme.typography.titleLarge
        } else {
            MaterialTheme.typography.bodyMedium
        },
        bottomText = if (isActive) {
            "work session"
        } else {
            "$remindersCount reminders in today's session"
        },
        icon = if (isActive) painterResource(R.drawable.ic_pause) else painterResource(R.drawable.ic_play_arrow),
        iconDescription = if (isActive) "stop" else "play",
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
            isActive = !isActive
        }
    )
}

@Composable
fun WorkSessionButtonContainer(
    topText: String,
    topTextStyle: TextStyle,
    bottomText: String,
    icon: Painter,
    iconDescription: String,
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
        modifier = Modifier
            .clip(shape)
            .clickable(onClick = onClick),
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = topText,
                    style = topTextStyle
                )
                Text(
                    text = bottomText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.contentColor)
                    .size(39.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = iconDescription,
                    tint = colors.containerColor
                )
            }
        }
    }
}