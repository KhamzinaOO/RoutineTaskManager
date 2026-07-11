package com.okhamzina.routinetaskmanager.core.notifications

import com.okhamzina.routinetaskmanager.featureReminder.domain.model.NotificationMode

fun NotificationMode.toReminderChannelId(): String {
    return when (this) {
        NotificationMode.SOUND -> {
            AppNotificationConstants.CHANNEL_NOTIFICATION_SOUND_ID
        }

        NotificationMode.VIBRATION -> {
            AppNotificationConstants.CHANNEL_NOTIFICATION_VIBRATION_ID
        }

        NotificationMode.MUTE -> {
            AppNotificationConstants.CHANNEL_NOTIFICATION_SILENT_ID
        }
    }
}