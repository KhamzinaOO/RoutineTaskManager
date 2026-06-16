package com.example.routinetaskmanager.core.notifications

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.routinetaskmanager.MainActivity
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.featureReminder.domain.useCase.WorkSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WorkSessionForegroundService : Service(), KoinComponent {

    private val workSessionManager: WorkSessionManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AppNotificationConstants.ACTION_STOP_WORK_SESSION_SERVICE -> {
                stopSession()
            }

            else -> {
                val startedAtMillis = intent?.getLongExtra(
                    AppNotificationConstants.EXTRA_WORK_SESSION_STARTED_AT,
                    0L
                )?.takeIf { it > 0L }
                    ?: workSessionManager.state.value.startedAtMillis
                    ?: System.currentTimeMillis()

                startForeground(
                    WORK_SESSION_NOTIFICATION_ID,
                    createNotification(startedAtMillis)
                )
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun stopSession() {
        serviceScope.launch {
            workSessionManager.endSession()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotification(startedAtMillis: Long): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            WORK_SESSION_CONTENT_REQUEST_CODE,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, WorkSessionForegroundService::class.java).apply {
            action = AppNotificationConstants.ACTION_STOP_WORK_SESSION_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            WORK_SESSION_STOP_REQUEST_CODE,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            this,
            AppNotificationConstants.CHANNEL_WORK_SESSION_ID
        )
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle("Work session")
            .setContentText("Timer is running")
            .setContentIntent(contentIntent)
            .setWhen(startedAtMillis)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .addAction(R.drawable.ic_pause, "Stop", stopPendingIntent)
            .build()
    }

    private companion object {
        const val WORK_SESSION_NOTIFICATION_ID = 9001
        const val WORK_SESSION_CONTENT_REQUEST_CODE = 9002
        const val WORK_SESSION_STOP_REQUEST_CODE = 9003
    }
}
