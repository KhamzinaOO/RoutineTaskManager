package com.example.routinetaskmanager.featureReminder.presentation.all_reminders.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.routinetaskmanager.R

@Composable
fun ReminderTypeIcon(
    isEnabled: Boolean,
    notificationEnabled: Boolean
) {
    val containerColor = when {
        !isEnabled -> MaterialTheme.colorScheme.surfaceVariant
        notificationEnabled -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when {
        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
        notificationEnabled -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val icon = when {
        !isEnabled -> painterResource(R.drawable.ic_alarm_disabled)
        notificationEnabled -> painterResource(R.drawable.ic_alarm_on)
        else -> painterResource(R.drawable.ic_alarm_off)
    }

    Surface(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = icon,
                contentDescription = null
            )
        }
    }
}