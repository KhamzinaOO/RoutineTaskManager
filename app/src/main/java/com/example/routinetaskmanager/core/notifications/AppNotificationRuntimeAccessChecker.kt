package com.example.routinetaskmanager.core.notifications

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
//TODO(Separate classes)
class AppNotificationRuntimeAccessChecker(
    private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    fun canPostNotifications(): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                true
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
        }.getOrDefault(false)
        //TODO(error catching)
    }

    fun canScheduleExactAlarms(): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                true
            } else {
                alarmManager.canScheduleExactAlarms()
            }
        }.getOrDefault(false)
    }
}
