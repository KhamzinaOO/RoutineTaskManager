package com.okhamzina.routinetaskmanager.featureReminder.application.session

import com.okhamzina.routinetaskmanager.core.error.AppError

sealed interface RestoreWorkSessionRuntimeResult {
    data object NotActive : RestoreWorkSessionRuntimeResult
    data object Restored : RestoreWorkSessionRuntimeResult

    data class Failed(
        val error: AppError
    ) : RestoreWorkSessionRuntimeResult
}
