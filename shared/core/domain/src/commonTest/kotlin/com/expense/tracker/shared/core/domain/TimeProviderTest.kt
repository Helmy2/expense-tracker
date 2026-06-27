package com.expense.tracker.shared.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeProviderTest {
    private val timeProvider = FixedTimeProvider(
        millis = 1_771_684_200_000L,
    )

    @Test
    fun todayReturnsCurrentLocalDate() {
        assertEquals(
            expected = LocalDate(2026, 2, 21),
            actual = timeProvider.today(),
        )
    }

    @Test
    fun currentYearMonthReturnsCurrentYearAndMonth() {
        assertEquals(
            expected = YearMonth(year = 2026, month = 2),
            actual = timeProvider.currentYearMonth(),
        )
    }

    @Test
    fun formatDateSupportsEnglishStyles() {
        assertEquals(
            expected = "2026-02-21",
            actual = timeProvider.formatDate(timeProvider.nowMillis(), DateFormatStyle.Short),
        )
        assertEquals(
            expected = "Feb 21, 2026",
            actual = timeProvider.formatDate(timeProvider.nowMillis(), DateFormatStyle.Medium),
        )
        assertEquals(
            expected = "February 21, 2026",
            actual = timeProvider.formatDate(timeProvider.nowMillis(), DateFormatStyle.Long),
        )
    }

    @Test
    fun formatDateTimeSupportsEnglishStyles() {
        assertEquals(
            expected = "2026-02-21 14:30",
            actual = timeProvider.formatDateTime(timeProvider.nowMillis(), DateTimeFormatStyle.Short),
        )
        assertEquals(
            expected = "Feb 21, 2026 14:30",
            actual = timeProvider.formatDateTime(timeProvider.nowMillis(), DateTimeFormatStyle.Medium),
        )
    }

    private class FixedTimeProvider(
        private val millis: Long,
    ) : TimeProvider {
        override fun nowMillis(): Long = millis

        override fun timeZone(): TimeZone = TimeZone.UTC
    }
}
