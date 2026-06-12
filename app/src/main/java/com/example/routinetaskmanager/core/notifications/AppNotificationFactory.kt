package com.example.routinetaskmanager.core.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.routinetaskmanager.MainActivity
import com.example.routinetaskmanager.R

class AppNotificationFactory(
    private val context : Context
) {
    fun createNotification(
        targetType: NotificationTargetType,
        targetId : Long,
        title : String,
        text : String?
    ) : Notification{
        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            putExtra(AppNotificationConstants.EXTRA_TARGET_TYPE, targetType.name)
            putExtra(AppNotificationConstants.EXTRA_TARGET_ID, targetId)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            buildContentRequestCode(targetType, targetId),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            context,
            AppNotificationConstants.CHANNEL_REMINDERS_ID
        )
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(text ?: title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text ?: title)
            )
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
    }


    private fun buildContentRequestCode(
        targetType: NotificationTargetType,
        targetId: Long
    ): Int {
        return "open-${targetType.name}-$targetId".hashCode()
    }
}