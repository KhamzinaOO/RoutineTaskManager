package com.example.routinetaskmanager.core.notifications.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.notifications.AppNotificationConstants

class AppNotificationChannels(
    private val context: Context
) {

    fun createChannels() {

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )

        val notificationSoundChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_NOTIFICATION_SOUND_ID,
            context.getString(R.string.notification_channel_sound_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_sound_description)
            enableVibration(true)
        }

        val notificationVibrationChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_NOTIFICATION_VIBRATION_ID,
            context.getString(R.string.notification_channel_vibration_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_vibration_description)
            setSound(null, null)
            enableVibration(true)
        }

        val notificationSilentChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_NOTIFICATION_SILENT_ID,
            context.getString(R.string.notification_channel_silent_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_silent_description)
            setSound(null, null)
            enableVibration(false)
        }

        val workSessionChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_WORK_SESSION_ID,
            context.getString(R.string.notification_channel_work_session_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_work_session_description)
            setSound(null, null)
            enableVibration(false)
        }

        notificationManager.createNotificationChannels(
            listOf(
                notificationSoundChannel,
                notificationVibrationChannel,
                notificationSilentChannel,
                workSessionChannel
            )
        )
    }
}