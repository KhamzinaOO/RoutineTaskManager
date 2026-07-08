package com.example.routinetaskmanager.core.error

import com.example.routinetaskmanager.core.presentation.model.UiText
import kotlinx.coroutines.CancellationException

suspend inline fun <T> runAppCatching(
    crossinline block: suspend () -> T
): AppResult<T, AppError> {
    return try {
        AppResult.Success(block())
    } catch (throwable: CancellationException) {
        throw throwable
    } catch (throwable: Throwable) {
        AppResult.Error(throwable.toAppError())
    }
}

suspend inline fun <T> runSuspendCatching(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (throwable: CancellationException) {
        throw throwable
    } catch (throwable: Throwable) {
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
