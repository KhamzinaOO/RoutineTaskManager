package com.example.routinetaskmanager.core.error

import com.example.routinetaskmanager.core.presentation.model.UiText

suspend inline fun <T> runAppCatching(
    crossinline block: suspend () -> T
): AppResult<T, AppError> {
    return try {
        AppResult.Success(block())
    } catch (throwable: Throwable) {
        AppResult.Error(throwable.toAppError())
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
