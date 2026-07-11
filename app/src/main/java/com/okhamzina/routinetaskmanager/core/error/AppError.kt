package com.okhamzina.routinetaskmanager.core.error

sealed interface AppError {
    data object Cancellation : AppError
    data object NotFound : AppError
    data object Storage : AppError
    data object Database : AppError
    data object PermissionDenied : AppError
    data object NotificationPermissionDenied : AppError
    data object ExactAlarmPermissionDenied : AppError
    data class AlarmSchedulingFailed(
        val throwable: Throwable? = null
    ) : AppError
    data object ForegroundServiceBlocked : AppError

    data class Validation(
        val reason: ValidationReason = ValidationReason.InvalidInput
    ) : AppError
    data class Unknown(val throwable: Throwable? = null) : AppError
}

sealed interface ValidationReason {
    data object InvalidInput : ValidationReason
}
