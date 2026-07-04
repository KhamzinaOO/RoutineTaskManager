package com.example.routinetaskmanager.core.error

import com.example.routinetaskmanager.core.presentation.model.UiText

sealed interface AppError {
    data object Cancellation : AppError
    data object NotFound : AppError
    data object Storage : AppError
    data object Database : AppError
    data object PermissionDenied : AppError
    data object NotificationPermissionDenied : AppError
    data object ExactAlarmPermissionDenied : AppError
    data object AlarmSchedulingFailed : AppError
    data object ForegroundServiceBlocked : AppError
    data class Validation(val message: UiText) : AppError
    data class Unknown(val throwable: Throwable? = null) : AppError
}
