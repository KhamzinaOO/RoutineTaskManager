package com.okhamzina.routinetaskmanager.core.notifications

import com.okhamzina.routinetaskmanager.core.error.AppError
import com.okhamzina.routinetaskmanager.core.error.AppResult
import com.okhamzina.routinetaskmanager.core.error.EmptyAppResult
import com.okhamzina.routinetaskmanager.featureReminder.application.notifications.RescheduleRemindersUseCase
import com.okhamzina.routinetaskmanager.featureReminder.application.session.WorkSessionManager

class RescheduleAllNotificationsUseCase(
    private val rescheduleRemindersUseCase: RescheduleRemindersUseCase,
    private val workSessionManager: WorkSessionManager
) {

    suspend operator fun invoke(): EmptyAppResult<AppError> {
        when (val regularResult = rescheduleRemindersUseCase()) {
            is AppResult.Error -> return AppResult.Error(regularResult.error)
            is AppResult.Success -> Unit
        }

        return when (val sessionResult = workSessionManager.rescheduleActiveSessionIfNeeded()) {
            is AppResult.Error -> AppResult.Error(sessionResult.error)
            is AppResult.Success -> AppResult.Success(Unit)
        }
    }
}
