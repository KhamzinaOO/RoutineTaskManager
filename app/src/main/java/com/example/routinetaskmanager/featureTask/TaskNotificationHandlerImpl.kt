package com.example.routinetaskmanager.featureTask

import com.example.routinetaskmanager.core.notifications.NotificationTargetType
import com.example.routinetaskmanager.core.notifications.ScheduledNotificationDao
import com.example.routinetaskmanager.core.notifications.TaskNotificationHandler

class TaskNotificationHandlerImpl(
    private val scheduledNotificationDao: ScheduledNotificationDao
) : TaskNotificationHandler {

    override suspend fun onNotificationTriggered(
        taskId: Long
    ) {
        scheduledNotificationDao.deleteByTarget(
            targetType = NotificationTargetType.TASK.name,
            targetId = taskId
        )
    }
}