package com.example.routinetaskmanager.core.error

sealed interface AppResult<out D, out E : AppError> {
    data class Success<out D>(val data: D) : AppResult<D, Nothing>
    data class Error<out E : AppError>(val error: E) : AppResult<Nothing, E>
}

inline fun <T, E : AppError, R> AppResult<T, E>.map(
    transform: (T) -> R
): AppResult<R, E> {
    return when (this) {
        is AppResult.Error -> AppResult.Error(error)
        is AppResult.Success -> AppResult.Success(transform(data))
    }
}

fun <T, E : AppError> AppResult<T, E>.asEmptyDataResult(): EmptyAppResult<E> {
    return map { }
}

inline fun <T, E : AppError> AppResult<T, E>.onSuccess(
    action: (T) -> Unit
): AppResult<T, E> {
    return when (this) {
        is AppResult.Error -> this
        is AppResult.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : AppError> AppResult<T, E>.onError(
    action: (E) -> Unit
): AppResult<T, E> {
    return when (this) {
        is AppResult.Error -> {
            action(error)
            this
        }
        is AppResult.Success -> this
    }
}

typealias EmptyAppResult<E> = AppResult<Unit, E>
