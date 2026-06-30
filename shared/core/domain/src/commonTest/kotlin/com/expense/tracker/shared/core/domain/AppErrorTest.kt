package com.expense.tracker.shared.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class AppErrorTest {

    @Test
    fun unknownErrorReturnsGenericMessage() {
        assertEquals(
            expected = "Something went wrong",
            actual = AppError.Unknown.asMessageText(),
        )
    }

    @Test
    fun messageErrorReturnsWrappedValue() {
        assertEquals(
            expected = "Network timeout",
            actual = AppError.Message("Network timeout").asMessageText(),
        )
    }

    @Test
    fun messageErrorWithEmptyStringReturnsEmpty() {
        assertEquals(
            expected = "",
            actual = AppError.Message("").asMessageText(),
        )
    }
}
