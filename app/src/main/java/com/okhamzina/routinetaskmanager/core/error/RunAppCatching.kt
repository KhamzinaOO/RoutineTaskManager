package com.okhamzina.routinetaskmanager.core.error

import com.okhamzina.routinetaskmanager.core.presentation.model.UiText
import kotlinx.coroutines.CancellationException

suspend inline fun <T> runAppCatching(
    errorReporter: ErrorReporter = NoOpErrorReporter,
    crossinline block: suspend () -> T
): AppResult<T, AppError> {
    return try {
        AppResult.Success(block())
    } catch (throwable: CancellationException) {
        throw throwable
    } catch (throwable: Throwable) {
        val error = throwable.toAppError()
        error.reportIfNeeded(errorReporter, throwable)
        AppResult.Error(error)
    }
}

suspend inline fun <T> runAppResultCatching(
    errorReporter: ErrorReporter = NoOpErrorReporter,
    crossinline block: suspend () -> AppResult<T, AppError>
): AppResult<T, AppError> {
    return try {
        block().also { result ->
            if (result is AppResult.Error) {
                result.error.reportIfNeeded(errorReporter)
            }
        }
    } catch (throwable: CancellationException) {
        throw throwable
    } catch (throwable: Throwable) {
        val error = throwable.toAppError()
        error.reportIfNeeded(errorReporter, throwable)
        AppResult.Error(error)
    }
}

suspend inline fun <T> runSuspendCatching(
    errorReporter: ErrorReporter = NoOpErrorReporter,
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (throwable: CancellationException) {
        throw throwable
    } catch (throwable: Throwable) {
        val error = throwable.toAppError()
        error.reportIfNeeded(errorReporter, throwable)
        Result.failure(throwable)
    }
}

inline fun <T> AppResult<T, AppError>.onErrorMessage(
    defaultMessage: UiText,
    action: (UiText) -> Unit
): AppResult<T, AppError> {
    return onError { error ->
        if (error.shouldShowMessage) {
            action(error.toUiText(defaultMessage))
        }
    }
}

fun AppError.reportIfNeeded(
    errorReporter: ErrorReporter,
    throwable: Throwable? = reportableThrowableOrNull()
) {
    if (shouldReport() && throwable != null) {
        errorReporter.record(throwable)
    }
}

private fun AppError.reportableThrowableOrNull(): Throwable? {
    return when (this) {
        is AppError.AlarmSchedulingFailed -> throwable
        is AppError.Unknown -> throwable
        else -> null
    }
}
