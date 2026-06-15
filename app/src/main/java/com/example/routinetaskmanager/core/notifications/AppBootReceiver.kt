package com.example.routinetaskmanager.core.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppBootReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleAllNotificationsUseCase: RescheduleAllNotificationsUseCase by inject()

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val exactAlarmPermissionChanged =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED

        val shouldReschedule =
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
                    intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                    intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
                    intent.action == Intent.ACTION_TIME_CHANGED ||
                    exactAlarmPermissionChanged

        if (!shouldReschedule) {
            return
        }

        if (exactAlarmPermissionChanged && !canScheduleExactAlarms(context)) {
            return
        }

        goAsync {
            rescheduleAllNotificationsUseCase()
        }
    }

    private fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }
}
