package com.example.routinetaskmanager.featureReminder.application.session

sealed interface ToggleWorkSessionResult {
    data class Started(
        val scheduledNotificationCount: Int
    ) : ToggleWorkSessionResult

    data object StartedWithoutReminders : ToggleWorkSessionResult

    data object Ended : ToggleWorkSessionResult

    data object ForegroundStartBlocked : ToggleWorkSessionResult

    data class StartFailed(
        val throwable: Throwable
    ) : ToggleWorkSessionResult

    data class EndFailed(
        val throwable: Throwable
    ) : ToggleWorkSessionResult
}
