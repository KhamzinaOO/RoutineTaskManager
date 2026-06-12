package com.example.routinetaskmanager.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class AppNotificationReceiver : BroadcastReceiver(), KoinComponent {

    private val appNotificationManager: AppNotificationManager by inject()
    private val notificationTriggerRouter: NotificationTriggerRouter by inject()
    private val scheduledNotificationDao: ScheduledNotificationDao by inject()

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != AppNotificationConstants.ACTION_SHOW_NOTIFICATION) {
            return
        }

        val targetTypeRaw = intent.getStringExtra(
            AppNotificationConstants.EXTRA_TARGET_TYPE
        ) ?: return

        val targetType = runCatching {
            NotificationTargetType.valueOf(targetTypeRaw)
        }.getOrNull() ?: return

        val targetId = intent.getLongExtra(
            AppNotificationConstants.EXTRA_TARGET_ID,
            -1L
        )

        if (targetId == -1L) {
            return
        }

        val scheduledAtMillis = intent.getLongExtra(
            AppNotificationConstants.EXTRA_SCHEDULED_AT,
            -1L
        )

        if (scheduledAtMillis == -1L) {
            return
        }

        val requestCode = intent.getIntExtra(
            AppNotificationConstants.EXTRA_REQUEST_CODE,
            Int.MIN_VALUE
        )

        goAsync {
            val payload = notificationTriggerRouter.buildPayloadOrNull(
                targetType = targetType,
                targetId = targetId,
                scheduledAtMillis = scheduledAtMillis
            )

            if (payload != null) {
                appNotificationManager.showNotification(payload)

                notificationTriggerRouter.onNotificationShown(
                    targetType = targetType,
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis
                )
            }

            if (requestCode != Int.MIN_VALUE) {
                scheduledNotificationDao.deleteByRequestCode(requestCode)
            }
        }
    }
}