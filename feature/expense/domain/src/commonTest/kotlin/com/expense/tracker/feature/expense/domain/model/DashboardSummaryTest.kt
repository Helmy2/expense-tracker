package com.expense.tracker.feature.expense.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardSummaryTest {
    @Test
    fun emptyListReturnsZeroTotals() {
        val summary = computeDashboard(emptyList())

        assertEquals(0.0, summary.totalBalance)
        assertEquals(0.0, summary.totalIncome)
        assertEquals(0.0, summary.totalExpenses)
    }

    @Test
    fun incomeOnlyReturnsPositiveBalance() {
        val transactions = listOf(
            Transaction("1", 100.0, TransactionType.INCOME, TransactionCategory.SALARY, "", 1L),
            Transaction("2", 50.0, TransactionType.INCOME, TransactionCategory.OTHER, "", 2L),
        )

        val summary = computeDashboard(transactions)

        assertEquals(150.0, summary.totalBalance)
        assertEquals(150.0, summary.totalIncome)
        assertEquals(0.0, summary.totalExpenses)
    }

    @Test
    fun expensesOnlyReturnsNegativeBalance() {
        val transactions = listOf(
            Transaction("1", 30.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "", 1L),
            Transaction("2", 20.0, TransactionType.EXPENSE, TransactionCategory.TRANSPORTATION, "", 2L),
        )

        val summary = computeDashboard(transactions)

        assertEquals(-50.0, summary.totalBalance)
        assertEquals(0.0, summary.totalIncome)
        assertEquals(50.0, summary.totalExpenses)
    }

    @Test
    fun mixedTransactionsComputeCorrectTotals() {
        val transactions = listOf(
            Transaction("1", 1000.0, TransactionType.INCOME, TransactionCategory.SALARY, "", 1L),
            Transaction("2", 200.0, TransactionType.EXPENSE, TransactionCategory.RENT, "", 2L),
            Transaction("3", 50.0, TransactionType.EXPENSE, TransactionCategory.FOOD, "", 3L),
            Transaction("4", 100.0, TransactionType.INCOME, TransactionCategory.OTHER, "", 4L),
        )

        val summary = computeDashboard(transactions)

        assertEquals(850.0, summary.totalBalance)
        assertEquals(1100.0, summary.totalIncome)
        assertEquals(250.0, summary.totalExpenses)
    }
}
