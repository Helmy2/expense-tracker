package com.expense.tracker.shared.core.domain

import kotlinx.coroutines.CancellationException

sealed interface Result<out T> {
    data class Success<T>(val value: T) : Result<T>
    data class Failure(val error: AppError) : Result<Nothing>
}

suspend inline fun <T> runSuspendCatching(
    crossinline block: suspend () -> T,
    crossinline onFailure: (Throwable) -> AppError = { AppError.Unknown }
): Result<T> = try {
    Result.Success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (throwable: Throwable) {
    Result.Failure(onFailure(throwable))
}
