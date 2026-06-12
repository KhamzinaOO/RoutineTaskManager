package com.example.routinetaskmanager.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class AndroidAppAlarmScheduler (
    private val context : Context
) : AppAlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun schedule(
        payload: NotificationPayload,
        requestCode: Int
    ) {
        if (payload.scheduledAtMillis <= System.currentTimeMillis()) {
            return
        }

        val pendingIntent = createPendingIntent(
            payload = payload,
            requestCode = requestCode
        )

        if (canScheduleExactAlarms()) {
            scheduleExact(
                triggerAtMillis = payload.scheduledAtMillis,
                pendingIntent = pendingIntent
            )
        } else {
            scheduleInexact(
                triggerAtMillis = payload.scheduledAtMillis,
                pendingIntent = pendingIntent
            )
        }
    }

    override fun cancel(
        requestCode: Int
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, AppNotificationReceiver::class.java).apply {
                action = AppNotificationConstants.ACTION_SHOW_NOTIFICATION
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun createPendingIntent(
        payload: NotificationPayload,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(
            context,
            AppNotificationReceiver::class.java
        ).apply {
            action = AppNotificationConstants.ACTION_SHOW_NOTIFICATION

            putExtra(
                AppNotificationConstants.EXTRA_TARGET_TYPE,
                payload.targetType.name
            )

            putExtra(
                AppNotificationConstants.EXTRA_TARGET_ID,
                payload.targetId
            )

            putExtra(
                AppNotificationConstants.EXTRA_TITLE,
                payload.title
            )

            putExtra(
                AppNotificationConstants.EXTRA_TEXT,
                payload.text
            )

            putExtra(
                AppNotificationConstants.EXTRA_SCHEDULED_AT,
                payload.scheduledAtMillis
            )
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleExact(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun scheduleInexact(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        return alarmManager.canScheduleExactAlarms()
    }
}