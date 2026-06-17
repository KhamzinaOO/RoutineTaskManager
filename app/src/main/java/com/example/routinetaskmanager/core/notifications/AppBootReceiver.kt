package com.example.routinetaskmanager.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.routinetaskmanager.core.coroutines.DispatcherProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppBootReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAllNotificationsUseCase: RescheduleAllNotificationsUseCase by inject()
    private val dispatcherProvider: DispatcherProvider by inject()

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val shouldReschedule =
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
                    intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                    intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
                    intent.action == Intent.ACTION_TIME_CHANGED

        if (!shouldReschedule) {
            return
        }

        goAsync(dispatcherProvider) {
            rescheduleAllNotificationsUseCase()
        }
    }
}
