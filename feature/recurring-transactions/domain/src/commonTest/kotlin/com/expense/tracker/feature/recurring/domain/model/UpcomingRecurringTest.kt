package com.expense.tracker.feature.recurring.domain.model

import com.expense.tracker.feature.expense.domain.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals

class UpcomingRecurringTest {
    @Test
    fun preservesAllFields() {
        val upcoming = UpcomingRecurring(
            templateId = "tmpl-1",
            amount = 1500.0,
            type = TransactionType.EXPENSE,
            category = "RENT",
            note = "Monthly rent",
            frequency = RecurringFrequency.MONTHLY,
            nextDueDateMillis = 1_720_000_000_000,
        )

        assertEquals("tmpl-1", upcoming.templateId)
        assertEquals(1500.0, upcoming.amount)
        assertEquals(TransactionType.EXPENSE, upcoming.type)
        assertEquals("RENT", upcoming.category)
        assertEquals("Monthly rent", upcoming.note)
        assertEquals(RecurringFrequency.MONTHLY, upcoming.frequency)
        assertEquals(1_720_000_000_000, upcoming.nextDueDateMillis)
    }

    @Test
    fun supportsIncomeType() {
        val upcoming = UpcomingRecurring(
            templateId = "tmpl-2",
            amount = 5000.0,
            type = TransactionType.INCOME,
            category = "SALARY",
            note = "Monthly salary",
            frequency = RecurringFrequency.MONTHLY,
            nextDueDateMillis = 1_720_000_000_000,
        )

        assertEquals(TransactionType.INCOME, upcoming.type)
        assertEquals("SALARY", upcoming.category)
    }

    @Test
    fun supportsAllFrequencies() {
        for (frequency in RecurringFrequency.entries) {
            val upcoming = UpcomingRecurring(
                templateId = "tmpl",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                category = "OTHER_EXPENSE",
                note = "",
                frequency = frequency,
                nextDueDateMillis = 1_720_000_000_000,
            )
            assertEquals(frequency, upcoming.frequency)
        }
    }
}
