package com.example.routinetaskmanager.core.notifications

interface AppAlarmScheduler {
    fun schedule(
        payload: NotificationPayload,
        requestCode : Int
    )

    fun cancel(
        requestCode : Int
    )
}