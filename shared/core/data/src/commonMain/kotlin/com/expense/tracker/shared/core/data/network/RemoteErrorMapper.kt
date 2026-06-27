package com.expense.tracker.shared.core.data.network

import com.expense.tracker.shared.core.domain.AppError
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

fun Throwable.toAppError(): AppError =
    when (this) {
        is ResponseException -> AppError.Message("Remote error: ${response.status.value}")
        is SerializationException -> AppError.Message("Serialization error")
        is IOException -> AppError.Message("Network error")
        else -> AppError.Unknown
    }
