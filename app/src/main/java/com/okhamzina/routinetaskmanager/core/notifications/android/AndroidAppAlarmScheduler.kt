package com.okhamzina.routinetaskmanager.core.notifications.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.okhamzina.routinetaskmanager.core.notifications.AppNotificationConstants
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduleResult
import com.okhamzina.routinetaskmanager.core.notifications.api.AlarmPrecision
import com.okhamzina.routinetaskmanager.core.notifications.api.AppAlarmScheduler
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType

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
        precision: AlarmPrecision,
        occurrenceKind: NotificationOccurrenceKind
    ): AppAlarmScheduleResult {
        if (scheduledAtMillis <= System.currentTimeMillis()) {
            return AppAlarmScheduleResult.TimeInPast
        }

        val pendingIntent = runCatching {
            createPendingIntent(
                targetType = targetType,
                targetId = targetId,
                scheduledAtMillis = scheduledAtMillis,
                requestCode = requestCode,
                occurrenceKind = occurrenceKind
            )
        }.getOrElse { throwable ->
            return AppAlarmScheduleResult.Failed(throwable)
        }

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

    private fun createPendingIntent(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        requestCode: Int,
        occurrenceKind: NotificationOccurrenceKind
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

            putExtra(
                AppNotificationConstants.EXTRA_OCCURRENCE_KIND,
                occurrenceKind.name
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
    ): AppAlarmScheduleResult {
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

    private fun scheduleExact(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ): AppAlarmScheduleResult {
        if (!permissionChecker.canScheduleExactAlarms()) {
            return AppAlarmScheduleResult.ExactAlarmAccessDenied
        }

        return runCatching {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            AppAlarmScheduleResult.Scheduled
        }.getOrElse { throwable ->
            if (throwable is SecurityException) {
                AppAlarmScheduleResult.ExactAlarmAccessDenied
            } else {
                AppAlarmScheduleResult.Failed(throwable)
            }
        }
    }

    private fun scheduleInexact(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ): AppAlarmScheduleResult {
        //think about use smth instead of .setAndAllowWhileIdle for better battery live
        return runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            AppAlarmScheduleResult.Scheduled
        }.getOrElse { throwable ->
            AppAlarmScheduleResult.Failed(throwable)
        }
    }
}
