package com.example.routinetaskmanager.featureReminder.application.session

sealed interface RestoreWorkSessionRuntimeResult {
    data object NotActive : RestoreWorkSessionRuntimeResult
    data object Restored : RestoreWorkSessionRuntimeResult

    data class Failed(
        val throwable: Throwable? = null
    ) : RestoreWorkSessionRuntimeResult
}