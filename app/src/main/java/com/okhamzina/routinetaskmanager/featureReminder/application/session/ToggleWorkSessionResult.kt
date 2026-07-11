package com.okhamzina.routinetaskmanager.featureReminder.application.session

import com.okhamzina.routinetaskmanager.core.error.AppError

sealed interface ToggleWorkSessionResult {
    data class Started(
        val scheduledNotificationCount: Int
    ) : ToggleWorkSessionResult

    data object StartedWithoutReminders : ToggleWorkSessionResult

    data object Ended : ToggleWorkSessionResult

    data object ForegroundStartBlocked : ToggleWorkSessionResult

    data class StartFailed(
        val error: AppError
    ) : ToggleWorkSessionResult

    data class EndFailed(
        val error: AppError
    ) : ToggleWorkSessionResult
}
