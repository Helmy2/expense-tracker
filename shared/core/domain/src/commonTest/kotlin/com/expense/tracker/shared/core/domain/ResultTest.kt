package com.expense.tracker.shared.core.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ResultTest {
    @Test
    fun wrapsSuccessfulValues() = runBlocking<Unit> {
        val result = runSuspendCatching(block = { "ok" })

        val success = assertIs<Result.Success<String>>(result)
        assertEquals("ok", success.value)
    }

    @Test
    fun mapsFailuresWithoutSwallowingCancellation() = runBlocking<Unit> {
        val result = runSuspendCatching(
            block = { error("boom") },
            onFailure = { AppError.Message(it.message ?: "unknown") }
        )

        val failure = assertIs<Result.Failure>(result)
        assertEquals(AppError.Message("boom"), failure.error)
    }

    @Test
    fun rethrowsCancellation() = runBlocking<Unit> {
        assertFailsWith<CancellationException> {
            runSuspendCatching(block = {
                throw CancellationException("stop")
            })
        }
    }
}