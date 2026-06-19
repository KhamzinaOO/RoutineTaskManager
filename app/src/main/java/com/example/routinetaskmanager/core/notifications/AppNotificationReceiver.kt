package com.example.routinetaskmanager.core.notifications

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import com.example.routinetaskmanager.core.notifications.AppNotificationConstants.EXTRA_REQUEST_CODE
import com.example.routinetaskmanager.data.local.notifications.ScheduledNotificationDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class AppNotificationReceiver : BroadcastReceiver(), KoinComponent {

    private val appNotificationManager: AppNotificationManager by inject()
    private val notificationTriggerRouter: NotificationTriggerRouter by inject()
    private val scheduledNotificationDao: ScheduledNotificationDao by inject()
    private val dispatcherProvider: DispatcherProvider by inject()

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

        if (!intent.hasExtra(EXTRA_REQUEST_CODE)) return
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)

        goAsync(dispatcherProvider) {
            val scheduledNotification = if (requestCode != Int.MIN_VALUE) {
                scheduledNotificationDao.getByRequestCode(requestCode)
            } else {
                null
            }

            val occurrenceKind = scheduledNotification
                ?.occurrenceKind
                ?.let { value ->
                    runCatching {
                        NotificationOccurrenceKind.valueOf(value)
                    }.getOrNull()
                }
                ?: NotificationOccurrenceKind.REGULAR

            val payload = notificationTriggerRouter.buildPayloadOrNull(
                targetType = targetType,
                targetId = targetId,
                scheduledAtMillis = scheduledAtMillis,
                occurrenceKind = occurrenceKind
            )

            if (payload != null) {
                val wasShown = appNotificationManager.showNotification(payload)
                //TODO: not the best delete case, need to rethink
                if (wasShown) {
                    notificationTriggerRouter.onNotificationShown(
                        targetType = targetType,
                        targetId = targetId,
                        scheduledAtMillis = scheduledAtMillis,
                        occurrenceKind = occurrenceKind
                    )
                    scheduledNotificationDao.deleteByRequestCode(requestCode)
                }
                if (!wasShown) {
                    Log.w(TAG, "Notification was not shown: targetType=$targetType targetId=$targetId requestCode=$requestCode")
                }
            }
            if (payload == null) {
                Log.w(TAG, "Notification payload is null: targetType=$targetType targetId=$targetId requestCode=$requestCode")
            }
        }
    }
}
