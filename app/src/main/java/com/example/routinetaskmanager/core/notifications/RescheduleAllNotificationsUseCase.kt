package com.example.routinetaskmanager.core.notifications

import com.example.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.application.session.WorkSessionManager

class RescheduleAllNotificationsUseCase(
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val workSessionManager: WorkSessionManager
) {

    suspend operator fun invoke() {
        rescheduleRemindersUseCase()
        workSessionManager.rescheduleActiveSessionIfNeeded()
    }
}
