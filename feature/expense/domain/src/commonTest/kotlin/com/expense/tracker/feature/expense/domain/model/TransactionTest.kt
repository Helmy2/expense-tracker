package com.expense.tracker.feature.expense.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionTest {
    @Test
    fun preservesAllFields() {
        val transaction = Transaction(
            id = "txn-1",
            amount = 42.50,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            note = "Lunch",
            createdAtMillis = 1_720_000_000_000,
        )

        assertEquals("txn-1", transaction.id)
        assertEquals(42.50, transaction.amount)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(TransactionCategory.FOOD, transaction.category)
        assertEquals("Lunch", transaction.note)
        assertEquals(1_720_000_000_000, transaction.createdAtMillis)
    }

    @Test
    fun allowsEmptyNote() {
        val transaction = Transaction(
            id = "txn-2",
            amount = 100.0,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            note = "",
            createdAtMillis = 1_720_000_000_000,
        )

        assertEquals("", transaction.note)
    }

    @Test
    fun supportsIncomeType() {
        val transaction = Transaction(
            id = "txn-3",
            amount = 500.0,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            note = "Monthly salary",
            createdAtMillis = 1_720_000_000_000,
        )

        assertEquals(TransactionType.INCOME, transaction.type)
    }

    @Test
    fun supportsExpenseType() {
        val transaction = Transaction(
            id = "txn-4",
            amount = 25.0,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.TRANSPORTATION,
            note = "Bus fare",
            createdAtMillis = 1_720_000_000_000,
        )

        assertEquals(TransactionType.EXPENSE, transaction.type)
    }
}
