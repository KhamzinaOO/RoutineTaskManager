package com.example.routinetaskmanager.core.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class AppNotificationManager(
    private val context: Context,
    private val notificationFactory: AppNotificationFactory
) {
    private val systemNotificationManager : NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun showNotification(
        targetType: NotificationTargetType,
        targetId : Long,
        title : String,
        text : String?
    ){
        if(!canShowNotifications()){
            return
        }

        val notification = notificationFactory.createNotification(
            targetType,
            targetId,
            title,
            text
        )

        systemNotificationManager.notify(
            buildNotificationId(
                targetType,
                targetId
            ),
            notification
        )
    }

    fun cancelNotification(
        targetType: NotificationTargetType,
        targetId: Long
    ){
        systemNotificationManager.cancel(
            buildNotificationId(
                targetType,
                targetId
            )
        )
    }

    private fun buildNotificationId(
        targetType: NotificationTargetType,
        targetId: Long
    ): Int {
        return "notification-${targetType.name}-$targetId".hashCode()
    }
    private fun canShowNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }


}