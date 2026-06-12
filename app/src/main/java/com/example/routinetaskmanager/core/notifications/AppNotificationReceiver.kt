package com.example.routinetaskmanager.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppNotificationReceiver : BroadcastReceiver(), KoinComponent {

    private val appNotificationManager: AppNotificationManager by inject()
    private val notificationRouter: NotificationRouter by inject()

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

        val title = intent.getStringExtra(
            AppNotificationConstants.EXTRA_TITLE
        ) ?: "Reminder"

        val text = intent.getStringExtra(
            AppNotificationConstants.EXTRA_TEXT
        )

        appNotificationManager.showNotification(
            targetType = targetType,
            targetId = targetId,
            title = title,
            text = text
        )

        goAsync {
            notificationRouter.onNotificationTriggered(
                targetType = targetType,
                targetId = targetId
            )
        }
    }
}