package com.okhamzina.routinetaskmanager.core.error

fun AppError.shouldReport(): Boolean {
    return when (this) {
        AppError.Cancellation,
        AppError.NotFound,
        AppError.PermissionDenied,
        AppError.NotificationPermissionDenied,
        AppError.ExactAlarmPermissionDenied,
        is AppError.Validation -> false

        AppError.Storage,
        AppError.Database,
        is AppError.AlarmSchedulingFailed,
        AppError.ForegroundServiceBlocked,
        is AppError.Unknown -> true
    }
}
