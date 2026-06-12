package com.example.routinetaskmanager.core.notifications

interface TaskNotificationHandler {

    suspend fun onNotificationTriggered(
        taskId: Long
    )
}

interface ReminderNotificationHandler {

    suspend fun onNotificationTriggered(
        reminderId: Long
    )
}