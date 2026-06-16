package com.example.routinetaskmanager.core.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class WorkSessionForegroundController(
    private val context: Context
) {

    fun start(startedAtMillis: Long) {
        val intent = Intent(context, WorkSessionForegroundService::class.java).apply {
            action = AppNotificationConstants.ACTION_START_WORK_SESSION_SERVICE
            putExtra(AppNotificationConstants.EXTRA_WORK_SESSION_STARTED_AT, startedAtMillis)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }

    fun stop() {
        context.stopService(Intent(context, WorkSessionForegroundService::class.java))
    }
}
