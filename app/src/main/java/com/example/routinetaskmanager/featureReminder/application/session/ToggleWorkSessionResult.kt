package com.example.routinetaskmanager.featureReminder.application.session

sealed interface ToggleWorkSessionResult {
    data class Started(
        val scheduledNotificationCount: Int,
        val wasRestart: Boolean
    ) : ToggleWorkSessionResult

    data object StartedWithoutReminders : ToggleWorkSessionResult

    data object Ended : ToggleWorkSessionResult

    data object ForegroundStartBlocked : ToggleWorkSessionResult

    data class Failed(
        val throwable: Throwable
    ) : ToggleWorkSessionResult
}