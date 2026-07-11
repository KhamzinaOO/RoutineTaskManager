package com.okhamzina.routinetaskmanager.core.error

import android.database.sqlite.SQLiteException
import com.okhamzina.routinetaskmanager.R
import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import java.io.IOException
import kotlinx.coroutines.CancellationException

fun Throwable.toAppError(): AppError {
    return when (this) {
        is CancellationException -> AppError.Cancellation
        is NoSuchElementException -> AppError.NotFound
        is IllegalArgumentException -> AppError.Validation(
            reason = ValidationReason.InvalidInput
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
        AppError.Storage,
        AppError.Database -> defaultMessage
        AppError.PermissionDenied -> UiText.StringResource(R.string.error_permission_denied)
        AppError.NotificationPermissionDenied -> UiText.StringResource(R.string.error_notification_permission_denied)
        AppError.ExactAlarmPermissionDenied -> UiText.StringResource(R.string.error_exact_alarm_permission_denied)
        is AppError.AlarmSchedulingFailed -> UiText.StringResource(R.string.error_alarm_scheduling_failed)
        AppError.ForegroundServiceBlocked -> UiText.StringResource(R.string.error_failed_start_work_session_service)
        is AppError.Validation -> reason.toUiText()
        is AppError.Unknown -> defaultMessage
    }
}


val AppError.shouldShowMessage: Boolean
    get() = this != AppError.Cancellation

private fun ValidationReason.toUiText(): UiText {
    return when (this) {
        ValidationReason.InvalidInput -> UiText.StringResource(R.string.error_invalid_input)
    }
}
