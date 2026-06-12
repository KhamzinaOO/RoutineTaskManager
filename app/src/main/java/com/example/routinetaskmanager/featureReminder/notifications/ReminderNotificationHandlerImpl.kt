package com.example.routinetaskmanager.featureReminder.notifications

import com.example.routinetaskmanager.core.notifications.ReminderNotificationHandler
import com.example.routinetaskmanager.featureReminder.domain.useCase.RescheduleRemindersUseCase

class ReminderNotificationHandlerImpl(
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase
) : ReminderNotificationHandler {

    override suspend fun onNotificationTriggered(
        reminderId: Long
    ) {
        rescheduleRemindersUseCase()
    }
}