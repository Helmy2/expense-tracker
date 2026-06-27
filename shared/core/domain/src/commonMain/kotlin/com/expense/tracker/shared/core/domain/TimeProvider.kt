package com.expense.tracker.shared.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

interface TimeProvider {
    fun nowMillis(): Long

    fun timeZone(): TimeZone = TimeZone.currentSystemDefault()

    fun today(): LocalDate = localDateTime(nowMillis()).date

    fun currentYearMonth(): YearMonth = today().toYearMonth()

    fun formatDate(
        millis: Long,
        style: DateFormatStyle = DateFormatStyle.Medium
    ): String = localDateTime(millis).date.toEnglishString(style)

    fun formatDateTime(
        millis: Long,
        style: DateTimeFormatStyle = DateTimeFormatStyle.Medium
    ): String {
        val dateTime = localDateTime(millis)
        val time = "${dateTime.hour.twoDigits()}:${dateTime.minute.twoDigits()}"
        return when (style) {
            DateTimeFormatStyle.Short -> "${dateTime.date.toEnglishString(DateFormatStyle.Short)} $time"
            DateTimeFormatStyle.Medium -> "${dateTime.date.toEnglishString(DateFormatStyle.Medium)} $time"
        }
    }

    private fun localDateTime(millis: Long) = Instant
        .fromEpochMilliseconds(millis)
        .toLocalDateTime(timeZone())
}

object SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

data class YearMonth(
    val year: Int,
    val month: Int,
)

enum class DateFormatStyle {
    Short,
    Medium,
    Long,
}

enum class DateTimeFormatStyle {
    Short,
    Medium,
}

fun LocalDate.toYearMonth(): YearMonth = YearMonth(
    year = year,
    month = monthIndex,
)

private fun LocalDate.toEnglishString(style: DateFormatStyle): String = when (style) {
    DateFormatStyle.Short -> "${year}-${monthIndex.twoDigits()}-${day.twoDigits()}"
    DateFormatStyle.Medium -> "${shortMonthNames[monthIndex - 1]} $day, $year"
    DateFormatStyle.Long -> "${longMonthNames[monthIndex - 1]} $day, $year"
}

private val LocalDate.monthIndex: Int
    get() = month.ordinal + 1

private fun Int.twoDigits(): String = if (this < 10) "0$this" else toString()

private val shortMonthNames = listOf(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
)

private val longMonthNames = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
)
