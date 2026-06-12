package com.example.routinetaskmanager.core.notifications


interface AppAlarmScheduler {

    fun schedule(
        targetType: NotificationTargetType,
        targetId: Long,
        scheduledAtMillis: Long,
        requestCode: Int
    )

    fun cancel(
        requestCode: Int
    )
}