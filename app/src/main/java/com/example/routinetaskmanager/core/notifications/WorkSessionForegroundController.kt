package com.example.routinetaskmanager.core.notifications

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class WorkSessionForegroundController(
    private val context: Context
) {

    fun start(startedAtMillis: Long): WorkSessionForegroundStartResult {
        val intent = Intent(context, WorkSessionForegroundService::class.java).apply {
            action = AppNotificationConstants.ACTION_START_WORK_SESSION_SERVICE
            putExtra(AppNotificationConstants.EXTRA_WORK_SESSION_STARTED_AT, startedAtMillis)
        }

        return runCatching {
            ContextCompat.startForegroundService(context, intent)
        }.fold(
            onSuccess = { WorkSessionForegroundStartResult.Started },
            onFailure = { throwable ->
                WorkSessionForegroundStartResult.Failed(throwable)
            }
        )
    }

    fun stop() {
        context.stopService(Intent(context, WorkSessionForegroundService::class.java))
    }
}

sealed interface WorkSessionForegroundStartResult {
    data object Started : WorkSessionForegroundStartResult
    data class Failed(val throwable: Throwable) : WorkSessionForegroundStartResult
}
