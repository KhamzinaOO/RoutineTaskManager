package com.example.routinetaskmanager.core.error

import android.database.sqlite.SQLiteException
import com.example.routinetaskmanager.R
import com.example.routinetaskmanager.core.presentation.model.UiText
import java.io.IOException
import kotlinx.coroutines.CancellationException

fun Throwable.toAppError(): AppError {
    return when (this) {
        is CancellationException -> AppError.Cancellation
        is NoSuchElementException -> AppError.NotFound
        is IllegalArgumentException -> AppError.Validation(
            message = message
                ?.takeIf { it.isNotBlank() }
                ?.let(UiText::DynamicString)
                ?: UiText.StringResource(R.string.error_invalid_input)
        )
        is SecurityException -> AppError.PermissionDenied
        is SQLiteException -> AppError.Database
        is IOException -> AppError.Storage
        else -> AppError.Unknown(this)
    }
}

fun AppError.toUiText(
    defaultMessage: UiText = UiText.StringResource(R.string.error_unknown)
): UiText {
    return when (this) {
        AppError.Cancellation -> UiText.StringResource(R.string.error_operation_cancelled)
        AppError.NotFound -> UiText.StringResource(R.string.error_item_not_found)
        AppError.Storage -> UiText.StringResource(R.string.error_storage)
        AppError.Database -> UiText.StringResource(R.string.error_database)
        AppError.PermissionDenied -> UiText.StringResource(R.string.error_permission_denied)
        AppError.NotificationPermissionDenied -> UiText.StringResource(R.string.error_notification_permission_denied)
        AppError.ExactAlarmPermissionDenied -> UiText.StringResource(R.string.error_exact_alarm_permission_denied)
        AppError.AlarmSchedulingFailed -> UiText.StringResource(R.string.error_alarm_scheduling_failed)
        AppError.ForegroundServiceBlocked -> UiText.StringResource(R.string.error_failed_start_work_session_service)
        is AppError.Validation -> message
        is AppError.Unknown -> defaultMessage
    }
}

val AppError.shouldShowMessage: Boolean
    get() = this != AppError.Cancellation
