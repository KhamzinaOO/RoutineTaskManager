package com.okhamzina.routinetaskmanager.core.notifications.android

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.notifications.AppNotificationConstants
import com.okhamzina.routinetaskmanager.core.notifications.NotificationTriggerRouter
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationOccurrenceKind
import com.okhamzina.routinetaskmanager.core.notifications.api.NotificationTargetType
import com.okhamzina.routinetaskmanager.core.notifications.domain.ScheduledNotificationRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppNotificationReceiver : BroadcastReceiver(), KoinComponent {

    private val appNotificationManager: AppNotificationManager by inject()
    private val notificationTriggerRouter: NotificationTriggerRouter by inject()
    private val scheduledNotificationRepository: ScheduledNotificationRepository by inject()
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

        if (!intent.hasExtra(AppNotificationConstants.EXTRA_REQUEST_CODE)) return
        val requestCode = intent.getIntExtra(AppNotificationConstants.EXTRA_REQUEST_CODE, 0)
        val intentOccurrenceKind = intent.getStringExtra(AppNotificationConstants.EXTRA_OCCURRENCE_KIND)
            ?.let { value ->
                runCatching {
                    NotificationOccurrenceKind.valueOf(value)
                }.getOrNull()
            }

        goAsync(dispatcherProvider) {
            val scheduledNotification = if (requestCode != Int.MIN_VALUE) {
                scheduledNotificationRepository.getByRequestCode(requestCode)
            } else {
                null
            }

            val occurrenceKind = scheduledNotification
                ?.occurrenceKind
                ?: intentOccurrenceKind
                ?: NotificationOccurrenceKind.REGULAR

            val payload = notificationTriggerRouter.buildPayloadOrNull(
                targetType = targetType,
                targetId = targetId,
                scheduledAtMillis = scheduledAtMillis,
                occurrenceKind = occurrenceKind
            )

            if (payload != null) {
                val wasShown = appNotificationManager.showNotification(payload)

                notificationTriggerRouter.onNotificationTriggered(
                    targetType = targetType,
                    targetId = targetId,
                    scheduledAtMillis = scheduledAtMillis,
                    occurrenceKind = occurrenceKind
                )
                scheduledNotificationRepository.deleteByRequestCode(requestCode)

                if (!wasShown) {
                    Log.w(ContentValues.TAG, "Notification was not shown: targetType=$targetType targetId=$targetId requestCode=$requestCode")
                }
            }
            if (payload == null) {
                Log.w(ContentValues.TAG, "Notification payload is null: targetType=$targetType targetId=$targetId requestCode=$requestCode")
            }
        }
    }
}
