package com.example.routinetaskmanager.core.notifications

class NotificationRouter(
    private val taskNotificationHandler: TaskNotificationHandler,
    private val reminderNotificationHandler: ReminderNotificationHandler
) {

    suspend fun onNotificationTriggered(
        targetType: NotificationTargetType,
        targetId: Long
    ) {
        when (targetType) {
            NotificationTargetType.TASK -> {
                taskNotificationHandler.onNotificationTriggered(targetId)
            }

            NotificationTargetType.REMINDER -> {
                reminderNotificationHandler.onNotificationTriggered(targetId)
            }
        }
    }
}