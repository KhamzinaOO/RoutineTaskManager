package com.example.routinetaskmanager.core.notifications

import com.example.routinetaskmanager.featureReminder.domain.useCase.RescheduleRemindersUseCase
import com.example.routinetaskmanager.featureReminder.domain.useCase.WorkSessionManager

class RescheduleAllNotificationsUseCase(
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val workSessionManager: WorkSessionManager
) {

    suspend operator fun invoke() {
        rescheduleRemindersUseCase()
        workSessionManager.rescheduleActiveSessionIfNeeded()
    }
}
