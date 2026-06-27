package com.expense.tracker.shared.core.testing

import com.expense.tracker.shared.core.domain.DateFormatStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeTimeProviderTest {
    @Test
    fun exposesFixedTime() {
        val timeProvider = FakeTimeProvider(current = 1_771_684_200_000L)

        assertEquals(1_771_684_200_000L, timeProvider.nowMillis())
        assertEquals("Feb 21, 2026", timeProvider.formatDate(timeProvider.nowMillis()))
    }

    @Test
    fun setNowMillisReplacesCurrentTime() {
        val timeProvider = FakeTimeProvider(current = 0L)

        timeProvider.setNowMillis(1_771_684_200_000L)

        assertEquals("2026-02-21", timeProvider.formatDate(timeProvider.nowMillis(), DateFormatStyle.Short))
    }

    @Test
    fun advanceByMovesCurrentTimeForward() {
        val timeProvider = FakeTimeProvider(current = 1_771_684_200_000L)

        timeProvider.advanceBy(60_000L)

        assertEquals(1_771_684_260_000L, timeProvider.nowMillis())
        assertEquals("Feb 21, 2026 14:31", timeProvider.formatDateTime(timeProvider.nowMillis()))
    }
}
