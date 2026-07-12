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
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType

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

        val builder = NotificationCompat.Builder(
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

        if (payload.targetType == NotificationTargetType.REMINDER && payload.occurrenceKey != null) {
            builder
                .addAction(
                    R.drawable.ic_check_box,
                    context.getString(R.string.action_complete),
                    buildActionPendingIntent(
                        payload = payload,
                        action = AppNotificationConstants.ACTION_COMPLETE_REMINDER
                    )
                )
                .addAction(
                    R.drawable.ic_clear,
                    context.getString(R.string.action_skip),
                    buildActionPendingIntent(
                        payload = payload,
                        action = AppNotificationConstants.ACTION_SKIP_REMINDER
                    )
                )
        }

        return builder.build()
    }

    private fun buildContentRequestCode(
        payload: NotificationPayload
    ): Int {
        return "open-${payload.targetType.name}-${payload.targetId}".hashCode()
    }

    private fun buildActionPendingIntent(
        payload: NotificationPayload,
        action: String
    ): PendingIntent {
        val intent = Intent(context, AppNotificationReceiver::class.java).apply {
            this.action = action
            putExtra(AppNotificationConstants.EXTRA_TARGET_TYPE, payload.targetType.name)
            putExtra(AppNotificationConstants.EXTRA_TARGET_ID, payload.targetId)
            putExtra(AppNotificationConstants.EXTRA_SCHEDULED_AT, payload.scheduledAtMillis)
            putExtra(AppNotificationConstants.EXTRA_OCCURRENCE_KIND, payload.occurrenceKind.name)
            putExtra(AppNotificationConstants.EXTRA_OCCURRENCE_KEY, payload.occurrenceKey)
        }

        return PendingIntent.getBroadcast(
            context,
            "${action}-${payload.occurrenceKey}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
