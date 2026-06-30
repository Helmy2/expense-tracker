package com.expense.tracker.shared.core.domain

sealed interface AppError {
    data object Unknown : AppError
    data class Message(val value: String) : AppError
}

fun AppError.asMessageText(): String = when (this) {
    AppError.Unknown -> "Something went wrong"
    is AppError.Message -> value
}
