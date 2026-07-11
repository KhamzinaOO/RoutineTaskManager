package com.okhamzina.routinetaskmanager.featureReminder.presentation.all_reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode

@Composable
fun ReminderNotificationRow(
    notificationMode: NotificationMode,
    notificationEnabled: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReminderMetaChip(
            icon = if (notificationEnabled) {
                painterResource(R.drawable.ic_alarm_on)
            } else {
                painterResource(R.drawable.ic_alarm_off)
            },
            text = if (notificationEnabled) {
                notificationMode.toUiLabel()
            } else {
                stringResource(R.string.notifications_off)
            }
        )
    }
}

@Composable
private fun ReminderMetaChip(
    icon: Painter,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 9.dp,
                vertical = 5.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = icon,
                contentDescription = null
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotificationMode.toUiLabel(): String {
    return when (this) {
        NotificationMode.SOUND -> stringResource(R.string.notification_mode_sound)
        NotificationMode.VIBRATION -> stringResource(R.string.notification_mode_vibration)
        NotificationMode.MUTE -> stringResource(R.string.notification_mode_silent)
    }
}
