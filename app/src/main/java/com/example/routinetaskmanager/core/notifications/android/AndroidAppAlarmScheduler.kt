package com.example.routinetaskmanager.core.notifications.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.routinetaskmanager.core.notifications.AppNotificationConstants
import com.example.routinetaskmanager.core.notifications.android.AppNotificationReceiver
import com.example.routinetaskmanager.core.notifications.android.AppNotificationRuntimeAccessChecker
import com.example.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.example.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.example.routinetaskmanager.core.notifications.api.NotificationTargetType

class AndroidAppAlarmScheduler(
    private val context: Context,
    private val permissionChecker: AppNotificationRuntimeAccessChecker
) : AppAlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun schedule(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        requestCode: Int,
        precision: AlarmPrecision
    ): Boolean {
        if (scheduledAtMillis <= System.currentTimeMillis()) {
            return false
        }

        val pendingIntent = runCatching {
            createPendingIntent(
                targetType = targetType,
                targetId = targetId,
                scheduledAtMillis = scheduledAtMillis,
                requestCode = requestCode
            )
        }.getOrNull() ?: return false

        return scheduleSafely(
            triggerAtMillis = scheduledAtMillis,
            pendingIntent = pendingIntent,
            precision = precision
        )
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
            runCatching {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    //TODO(add occurrence kind)
    private fun createPendingIntent(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(
            context,
            AppNotificationReceiver::class.java
        ).apply {
            action = AppNotificationConstants.ACTION_SHOW_NOTIFICATION

            putExtra(
                AppNotificationConstants.EXTRA_TARGET_TYPE,
                targetType.name
            )

            putExtra(
                AppNotificationConstants.EXTRA_TARGET_ID,
                targetId
            )

            putExtra(
                AppNotificationConstants.EXTRA_SCHEDULED_AT,
                scheduledAtMillis
            )

            putExtra(
                AppNotificationConstants.EXTRA_REQUEST_CODE,
                requestCode
            )
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleSafely(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
        precision: AlarmPrecision
    ): Boolean {
        return when (precision) {
            AlarmPrecision.INEXACT -> scheduleInexact(
                triggerAtMillis = triggerAtMillis,
                pendingIntent = pendingIntent
            )

            AlarmPrecision.EXACT -> scheduleExact(
                triggerAtMillis = triggerAtMillis,
                pendingIntent = pendingIntent
            )
        }
    }

    // TODO: permission!!!!
    private fun scheduleExact(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ): Boolean {
        if (!permissionChecker.canScheduleExactAlarms()) {
            return false
        }

        return runCatching {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            true
        }.getOrDefault(false)
    }

    private fun scheduleInexact(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ): Boolean {
        //think about use smth instead of .setAndAllowWhileIdle for better battery live
        return runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            true
        }.getOrDefault(false)
    }
}