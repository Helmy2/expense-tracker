package com.expense.tracker.feature.recurring.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class RecurringFrequencyTest {
    @Test
    fun hasExpectedDailyValue() {
        assertEquals("DAILY", RecurringFrequency.DAILY.name)
    }

    @Test
    fun hasExpectedWeeklyValue() {
        assertEquals("WEEKLY", RecurringFrequency.WEEKLY.name)
    }

    @Test
    fun hasExpectedMonthlyValue() {
        assertEquals("MONTHLY", RecurringFrequency.MONTHLY.name)
    }

    @Test
    fun hasExpectedYearlyValue() {
        assertEquals("YEARLY", RecurringFrequency.YEARLY.name)
    }

    @Test
    fun hasFourValues() {
        assertEquals(4, RecurringFrequency.entries.size)
    }

    @Test
    fun parsesFromString() {
        assertEquals(RecurringFrequency.DAILY, RecurringFrequency.valueOf("DAILY"))
        assertEquals(RecurringFrequency.WEEKLY, RecurringFrequency.valueOf("WEEKLY"))
        assertEquals(RecurringFrequency.MONTHLY, RecurringFrequency.valueOf("MONTHLY"))
        assertEquals(RecurringFrequency.YEARLY, RecurringFrequency.valueOf("YEARLY"))
    }
}
