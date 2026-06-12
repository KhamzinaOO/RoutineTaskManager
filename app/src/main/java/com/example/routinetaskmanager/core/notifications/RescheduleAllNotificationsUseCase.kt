package com.example.routinetaskmanager.core.notifications

import com.example.routinetaskmanager.featureReminder.domain.useCase.RescheduleRemindersUseCase

//TODO() Reschedule tasks

class RescheduleAllNotificationsUseCase(
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase
) {

    suspend operator fun invoke() {
        rescheduleRemindersUseCase()
    }
}