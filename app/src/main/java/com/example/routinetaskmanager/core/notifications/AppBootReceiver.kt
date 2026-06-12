package com.example.routinetaskmanager.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppBootReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAllNotificationsUseCase: RescheduleAllNotificationsUseCase by inject()

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val shouldReschedule =
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
                    intent.action == Intent.ACTION_MY_PACKAGE_REPLACED

        if (!shouldReschedule) {
            return
        }

        goAsync {
            rescheduleAllNotificationsUseCase()
        }
    }
}