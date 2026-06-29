package com.expense.tracker.shared.di

import kotlinx.coroutines.CancellationException

/**
 * Defensive wrapper for the iOS bridge `*OrThrow` extensions.
 *
 * SKIE converts `RuntimeException` thrown from a `suspend` function into a
 * Swift `Error` resumed on the awaiting continuation, but other Kotlin
 * exception types (e.g. `IllegalStateException`, `NullPointerException`,
 * `IllegalArgumentException`) are not always converted. When an unhandled
 * non-`CancellationException` reaches the top of a `suspend` chain on the
 * iOS coroutine dispatcher, the process aborts via
 * `propagateExceptionFinalResort` because no `CoroutineExceptionHandler`
 * catches it.
 *
 * This helper guarantees that every `*OrThrow` extension surfaces a
 * `RuntimeException` to SKIE. `CancellationException` is rethrown unchanged
 * so structured concurrency continues to work.
 */
internal suspend inline fun <T> safeOrThrow(operation: String, block: () -> T): T = try {
    block()
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (throwable: Throwable) {
    val typeName = throwable::class.simpleName
        ?: throwable::class.qualifiedName
        ?: "Throwable"
    throw RuntimeException(
        "$operation failed: $typeName: ${throwable.message}",
        throwable,
    )
}
