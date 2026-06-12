package com.example.routinetaskmanager.core.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class AppNotificationManager(
    private val context: Context,
    private val notificationFactory: AppNotificationFactory
) {

    private val systemNotificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun showNotification(
        payload: NotificationPayload
    ) {
        if (!canShowNotifications()) {
            return
        }

        val notification = notificationFactory.createNotification(payload)

        systemNotificationManager.notify(
            buildNotificationId(payload),
            notification
        )
    }

    fun cancelNotification(
        targetType: NotificationTargetType,
        targetId: Long
    ) {
        systemNotificationManager.cancel(
            "notification-${targetType.name}-$targetId".hashCode()
        )
    }

    private fun canShowNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildNotificationId(
        payload: NotificationPayload
    ): Int {
        return when (payload.targetType) {
            NotificationTargetType.REMINDER -> {
                "notification-REMINDER-${payload.targetId}".hashCode()
            }

            NotificationTargetType.TASK -> {
                "notification-TASK-${payload.targetId}".hashCode()
            }
        }
    }
}