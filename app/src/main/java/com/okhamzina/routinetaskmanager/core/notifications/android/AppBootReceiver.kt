package com.okhamzina.routinetaskmanager.core.notifications.android

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.okhamzina.routinetaskmanager.core.coroutines.DispatcherProvider
import com.okhamzina.routinetaskmanager.core.error.ErrorReporter
import com.okhamzina.routinetaskmanager.core.error.runAppResultCatching
import com.okhamzina.routinetaskmanager.core.notifications.RescheduleAllNotificationsUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppBootReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAllNotificationsUseCase: RescheduleAllNotificationsUseCase by inject()
    private val dispatcherProvider: DispatcherProvider by inject()
    private val errorReporter: ErrorReporter by inject()

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val shouldReschedule =
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
                    intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                    intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
                    intent.action == Intent.ACTION_TIME_CHANGED ||
                    intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED

        if (!shouldReschedule) {
            return
        }

        goAsync(dispatcherProvider) {
            runAppResultCatching(errorReporter) {
                rescheduleAllNotificationsUseCase()
            }
        }
    }
}
