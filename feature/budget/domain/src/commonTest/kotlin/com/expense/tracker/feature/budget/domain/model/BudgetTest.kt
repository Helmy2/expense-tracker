package com.expense.tracker.feature.budget.domain.model

import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BudgetTest {

    @Test
    fun budgetConstruction() {
        val budget = Budget(
            id = "b1",
            category = ExpenseCategory.FOOD,
            monthlyLimit = 500.0,
            createdAtMillis = 1000L,
            updatedAtMillis = 2000L,
        )
        assertEquals("b1", budget.id)
        assertEquals(ExpenseCategory.FOOD, budget.category)
        assertEquals(500.0, budget.monthlyLimit)
        assertEquals(1000L, budget.createdAtMillis)
        assertEquals(2000L, budget.updatedAtMillis)
    }

    // -- computeBudgetStatus tests --

    @Test
    fun statusUnder75() {
        assertEquals(BudgetStatus.UNDER_75, computeBudgetStatus(0.0))
        assertEquals(BudgetStatus.UNDER_75, computeBudgetStatus(0.5))
        assertEquals(BudgetStatus.UNDER_75, computeBudgetStatus(0.74))
    }

    @Test
    fun statusBetween75And90() {
        assertEquals(BudgetStatus.BETWEEN_75_90, computeBudgetStatus(0.75))
        assertEquals(BudgetStatus.BETWEEN_75_90, computeBudgetStatus(0.82))
        assertEquals(BudgetStatus.BETWEEN_75_90, computeBudgetStatus(0.8999))
    }

    @Test
    fun statusOver90() {
        assertEquals(BudgetStatus.OVER_90, computeBudgetStatus(0.90))
        assertEquals(BudgetStatus.OVER_90, computeBudgetStatus(0.95))
        assertEquals(BudgetStatus.OVER_90, computeBudgetStatus(1.0))
    }

    @Test
    fun statusOverBudget() {
        assertEquals(BudgetStatus.OVER_BUDGET, computeBudgetStatus(1.01))
        assertEquals(BudgetStatus.OVER_BUDGET, computeBudgetStatus(1.5))
        assertEquals(BudgetStatus.OVER_BUDGET, computeBudgetStatus(2.0))
    }

    @Test
    fun exactThresholds() {
        assertEquals(BudgetStatus.UNDER_75, computeBudgetStatus(0.7499))
        assertEquals(BudgetStatus.BETWEEN_75_90, computeBudgetStatus(0.75))
        assertEquals(BudgetStatus.BETWEEN_75_90, computeBudgetStatus(0.8999))
        assertEquals(BudgetStatus.OVER_90, computeBudgetStatus(0.90))
        assertEquals(BudgetStatus.OVER_90, computeBudgetStatus(1.0))
        assertEquals(BudgetStatus.OVER_BUDGET, computeBudgetStatus(1.0001))
    }

    // -- withSpending tests --

    @Test
    fun withSpendingBasicComputation() {
        val budget = Budget(
            id = "b1",
            category = ExpenseCategory.FOOD,
            monthlyLimit = 400.0,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
        val result = budget.withSpending(100.0)
        assertEquals(100.0, result.spentAmount)
        assertEquals(300.0, result.remainingAmount)
        assertEquals(0.25, result.percentage)
        assertEquals(BudgetStatus.UNDER_75, result.status)
    }

    @Test
    fun withSpendingOverBudget() {
        val budget = Budget(
            id = "b1",
            category = ExpenseCategory.TRANSPORTATION,
            monthlyLimit = 200.0,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
        val result = budget.withSpending(250.0)
        assertEquals(250.0, result.spentAmount)
        assertEquals(-50.0, result.remainingAmount)
        assertEquals(1.25, result.percentage)
        assertEquals(BudgetStatus.OVER_BUDGET, result.status)
    }

    @Test
    fun withSpendingZeroLimit() {
        val budget = Budget(
            id = "b1",
            category = ExpenseCategory.OTHER_EXPENSE,
            monthlyLimit = 0.0,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
        val result = budget.withSpending(50.0)
        assertEquals(0.0, result.percentage)
        assertEquals(BudgetStatus.UNDER_75, result.status)
        assertEquals(-50.0, result.remainingAmount)
    }

    @Test
    fun withSpendingExactThresholds() {
        val budget = Budget(
            id = "b1",
            category = ExpenseCategory.SHOPPING,
            monthlyLimit = 1000.0,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )

        val at75 = budget.withSpending(750.0)
        assertEquals(BudgetStatus.BETWEEN_75_90, at75.status)

        val at90 = budget.withSpending(900.0)
        assertEquals(BudgetStatus.OVER_90, at90.status)

        val at100 = budget.withSpending(1000.0)
        assertEquals(BudgetStatus.OVER_90, at100.status)

        val over = budget.withSpending(1001.0)
        assertEquals(BudgetStatus.OVER_BUDGET, over.status)
    }

    @Test
    fun withSpendingZeroSpent() {
        val budget = Budget(
            id = "b1",
            category = ExpenseCategory.ENTERTAINMENT,
            monthlyLimit = 300.0,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
        val result = budget.withSpending(0.0)
        assertEquals(0.0, result.spentAmount)
        assertEquals(300.0, result.remainingAmount)
        assertEquals(0.0, result.percentage)
        assertEquals(BudgetStatus.UNDER_75, result.status)
    }
}
