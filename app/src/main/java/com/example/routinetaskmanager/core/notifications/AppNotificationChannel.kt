package com.example.routinetaskmanager.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class AppNotificationChannel(
    private val context : Context
) {
    fun createChannels(){
        val channel = NotificationChannel(
            AppNotificationConstants.CHANNEL_REMINDERS_ID,
            AppNotificationConstants.CHANNEL_REMINDERS_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for tasks and reminders"
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )

        notificationManager.createNotificationChannel(channel)
    }
}