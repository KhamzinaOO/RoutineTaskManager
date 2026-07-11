package com.okhamzina.routinetaskmanager.core.notifications.android

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.okhamzina.routinetaskmanager.MainActivity
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.notifications.AppNotificationConstants
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationPayload

class AppNotificationFactory(
    private val context: Context
) {

    fun createNotification(
        payload: NotificationPayload
    ): Notification {
        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            putExtra(AppNotificationConstants.EXTRA_TARGET_TYPE, payload.targetType.name)
            putExtra(AppNotificationConstants.EXTRA_TARGET_ID, payload.targetId)
            putExtra(AppNotificationConstants.EXTRA_SCHEDULED_AT, payload.scheduledAtMillis)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            buildContentRequestCode(payload),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            context,
            payload.channelId
        )
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(payload.title)
            .setContentText(payload.text ?: payload.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(payload.text ?: payload.title)
            )
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setGroup(AppNotificationConstants.REMINDER_GROUP_KEY)
            .build()
    }

    private fun buildContentRequestCode(
        payload: NotificationPayload
    ): Int {
        return "open-${payload.targetType.name}-${payload.targetId}".hashCode()
    }
}