package com.example.routinetaskmanager.core.notifications

import android.app.NotificationManager
import android.content.Context

class AppNotificationManager(
    private val context: Context,
    private val notificationFactory: AppNotificationFactory,
    private val permissionChecker: AppNotificationPermissionChecker
) {

    private val systemNotificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun showNotification(
        payload: NotificationPayload
    ): Boolean {
        if (!permissionChecker.canPostNotifications()) {
            return false
        }

        return runCatching {
            val notification = notificationFactory.createNotification(payload)

            systemNotificationManager.notify(
                buildNotificationId(payload),
                notification
            )

            true
        }.getOrDefault(false)
    }

    fun cancelNotification(
        targetType: NotificationTargetType,
        targetId: Long
    ) {
        runCatching {
            systemNotificationManager.cancel(
                "notification-${targetType.name}-$targetId".hashCode()
            )
        }
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
