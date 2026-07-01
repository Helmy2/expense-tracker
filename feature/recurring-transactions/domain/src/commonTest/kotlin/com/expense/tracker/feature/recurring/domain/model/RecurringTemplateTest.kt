package com.expense.tracker.feature.recurring.domain.model

import com.expense.tracker.feature.expense.domain.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecurringTemplateTest {
    @Test
    fun preservesAllFields() {
        val template = RecurringTemplate(
            id = "tmpl-1",
            amount = 1500.0,
            type = TransactionType.EXPENSE,
            category = "RENT",
            note = "Monthly rent",
            frequency = RecurringFrequency.MONTHLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertEquals("tmpl-1", template.id)
        assertEquals(1500.0, template.amount)
        assertEquals(TransactionType.EXPENSE, template.type)
        assertEquals("RENT", template.category)
        assertEquals("Monthly rent", template.note)
        assertEquals(RecurringFrequency.MONTHLY, template.frequency)
        assertEquals(1_720_000_000_000, template.startDateMillis)
        assertNull(template.endDateMillis)
        assertEquals(false, template.isPaused)
        assertNull(template.lastGeneratedDateMillis)
        assertEquals(1_720_000_000_000, template.createdAtMillis)
        assertEquals(1_720_000_000_000, template.updatedAtMillis)
    }

    @Test
    fun supportsWeeklyFrequency() {
        val template = RecurringTemplate(
            id = "tmpl-2",
            amount = 200.0,
            type = TransactionType.EXPENSE,
            category = "FOOD",
            note = "Weekly groceries",
            frequency = RecurringFrequency.WEEKLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertEquals(RecurringFrequency.WEEKLY, template.frequency)
    }

    @Test
    fun supportsDailyFrequency() {
        val template = RecurringTemplate(
            id = "tmpl-3",
            amount = 5.0,
            type = TransactionType.EXPENSE,
            category = "TRANSPORTATION",
            note = "Daily bus pass",
            frequency = RecurringFrequency.DAILY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertEquals(RecurringFrequency.DAILY, template.frequency)
    }

    @Test
    fun supportsYearlyFrequency() {
        val template = RecurringTemplate(
            id = "tmpl-4",
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            category = "HEALTHCARE",
            note = "Annual insurance",
            frequency = RecurringFrequency.YEARLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertEquals(RecurringFrequency.YEARLY, template.frequency)
    }

    @Test
    fun supportsPausedState() {
        val template = RecurringTemplate(
            id = "tmpl-5",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = "OTHER_EXPENSE",
            note = "Paused subscription",
            frequency = RecurringFrequency.MONTHLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = true,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertTrue(template.isPaused)
    }

    @Test
    fun supportsEndDate() {
        val template = RecurringTemplate(
            id = "tmpl-6",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            category = "UTILITIES",
            note = "Limited subscription",
            frequency = RecurringFrequency.MONTHLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = 1_780_000_000_000,
            isPaused = false,
            lastGeneratedDateMillis = 1_720_000_000_000,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertEquals(1_780_000_000_000, template.endDateMillis)
        assertEquals(1_720_000_000_000, template.lastGeneratedDateMillis)
    }

    @Test
    fun supportsIncomeCategory() {
        val template = RecurringTemplate(
            id = "tmpl-7",
            amount = 5000.0,
            type = TransactionType.INCOME,
            category = "SALARY",
            note = "Monthly salary",
            frequency = RecurringFrequency.MONTHLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertEquals(TransactionType.INCOME, template.type)
        assertEquals("SALARY", template.category)
    }

    @Test
    fun categoryIsStoredAsString() {
        val template = RecurringTemplate(
            id = "tmpl-8",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = "FOOD",
            note = "",
            frequency = RecurringFrequency.WEEKLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        assertEquals("FOOD", template.category)
    }
}
