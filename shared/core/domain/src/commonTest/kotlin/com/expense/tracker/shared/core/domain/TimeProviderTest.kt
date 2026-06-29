package com.expense.tracker.shared.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
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
    fun yearMonthRangeMillisReturnsHalfOpenRangeForCurrentMonth() {
        val zone = TimeZone.UTC
        val expectedStart = LocalDateTime(2026, 2, 1, 0, 0)
            .toInstant(zone)
            .toEpochMilliseconds()
        val expectedEnd = LocalDateTime(2026, 3, 1, 0, 0)
            .toInstant(zone)
            .toEpochMilliseconds()

        val range = timeProvider.yearMonthRangeMillis(YearMonth(year = 2026, month = 2))

        assertEquals(expectedStart, range.first)
        assertEquals(expectedEnd, range.last + 1)
    }

    @Test
    fun yearMonthRangeMillisWrapsDecemberToJanuaryOfNextYear() {
        val zone = TimeZone.UTC
        val expectedStart = LocalDateTime(2026, 12, 1, 0, 0)
            .toInstant(zone)
            .toEpochMilliseconds()
        val expectedEnd = LocalDateTime(2027, 1, 1, 0, 0)
            .toInstant(zone)
            .toEpochMilliseconds()

        val range = timeProvider.yearMonthRangeMillis(YearMonth(year = 2026, month = 12))

        assertEquals(expectedStart, range.first)
        assertEquals(expectedEnd, range.last + 1)
    }

    @Test
    fun yearMonthRangeMillisIsContiguousAcrossMonths() {
        val jan = timeProvider.yearMonthRangeMillis(YearMonth(year = 2026, month = 1))
        val feb = timeProvider.yearMonthRangeMillis(YearMonth(year = 2026, month = 2))

        // The end of January equals the start of February (half-open boundary).
        assertEquals(jan.last + 1, feb.first)
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
