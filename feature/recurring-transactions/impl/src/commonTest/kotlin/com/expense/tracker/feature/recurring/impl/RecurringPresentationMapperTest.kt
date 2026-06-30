package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring
import com.expense.tracker.shared.core.testing.FakeTimeProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecurringPresentationMapperTest {
    private val timeProvider = FakeTimeProvider(current = 1700000000000L)
    private val mapper = RecurringPresentationMapper(timeProvider)

    @Test
    fun formatDateReturnsFormattedString() {
        val result = mapper.formatDate(1700000000000L)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatAmountPositive() {
        val result = mapper.formatAmount(1500.0)
        assertEquals("$1500.00", result)
    }

    @Test
    fun formatAmountZero() {
        val result = mapper.formatAmount(0.0)
        assertEquals("$0.00", result)
    }

    @Test
    fun formatSignedAmountIncome() {
        val result = mapper.formatSignedAmount(1500.0, isIncome = true)
        assertEquals("+ $1500.00", result)
    }

    @Test
    fun formatSignedAmountExpense() {
        val result = mapper.formatSignedAmount(45.50, isIncome = false)
        assertEquals("- $45.50", result)
    }

    @Test
    fun toTemplateUiMapsCorrectly() {
        val template = RecurringTemplate(
            id = "rt-1",
            amount = 1500.0,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            note = "Monthly salary",
            frequency = RecurringFrequency.MONTHLY,
            startDateMillis = 1700000000000L,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1690000000000L,
            updatedAtMillis = 1690000000000L,
        )

        val ui = mapper.toTemplateUi(template)

        assertEquals("rt-1", ui.id)
        assertTrue(ui.formattedAmount.contains("+"))
        assertEquals(TransactionCategory.SALARY, ui.category)
        assertEquals("Monthly", ui.frequencyLabel)
        assertFalse(ui.isPaused)
        assertTrue(ui.isIncome)
    }

    @Test
    fun toTemplateUiPaused() {
        val template = RecurringTemplate(
            id = "rt-2",
            amount = 45.0,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.ENTERTAINMENT,
            note = "Netflix",
            frequency = RecurringFrequency.MONTHLY,
            startDateMillis = 1700000000000L,
            endDateMillis = null,
            isPaused = true,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1690000000000L,
            updatedAtMillis = 1690000000000L,
        )

        val ui = mapper.toTemplateUi(template)

        assertTrue(ui.isPaused)
        assertFalse(ui.isIncome)
    }

    @Test
    fun toUpcomingUiMapsCorrectly() {
        val upcoming = UpcomingRecurring(
            templateId = "rt-1",
            amount = 1500.0,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            note = "Monthly salary",
            frequency = RecurringFrequency.MONTHLY,
            nextDueDateMillis = 1700000000000L,
        )

        val ui = mapper.toUpcomingUi(upcoming)

        assertEquals("rt-1", ui.templateId)
        assertTrue(ui.formattedAmount.contains("+"))
        assertEquals(TransactionCategory.SALARY, ui.category)
        assertTrue(ui.isIncome)
    }

    @Test
    fun frequencyToLabelDaily() {
        assertEquals("Daily", RecurringFrequency.DAILY.toLabel())
    }

    @Test
    fun frequencyToLabelWeekly() {
        assertEquals("Weekly", RecurringFrequency.WEEKLY.toLabel())
    }

    @Test
    fun frequencyToLabelMonthly() {
        assertEquals("Monthly", RecurringFrequency.MONTHLY.toLabel())
    }

    @Test
    fun frequencyToLabelYearly() {
        assertEquals("Yearly", RecurringFrequency.YEARLY.toLabel())
    }
}
