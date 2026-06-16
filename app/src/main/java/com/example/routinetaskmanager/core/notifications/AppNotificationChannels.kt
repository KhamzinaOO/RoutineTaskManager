package com.example.routinetaskmanager.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class AppNotificationChannels(
    private val context: Context
) {

    fun createChannels() {

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )

        val notificationSoundChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_NOTIFICATION_SOUND_ID,
            "notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "notification notifications with sound"
            enableVibration(true)
        }

        val notificationVibrationChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_NOTIFICATION_VIBRATION_ID,
            "notifications vibration",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "notification notifications with vibration"
            setSound(null, null)
            enableVibration(true)
        }

        val notificationSilentChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_NOTIFICATION_SILENT_ID,
            "notifications silent",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Silent notification notifications"
            setSound(null, null)
            enableVibration(false)
        }

        val workSessionChannel = NotificationChannel(
            AppNotificationConstants.CHANNEL_WORK_SESSION_ID,
            "work session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active work session timer"
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
