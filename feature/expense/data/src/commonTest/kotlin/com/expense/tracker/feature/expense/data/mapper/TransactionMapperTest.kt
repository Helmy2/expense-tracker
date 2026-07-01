package com.expense.tracker.feature.expense.data.mapper

import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.data.entity.TransactionEntity
import com.expense.tracker.feature.expense.data.mapper.toDomain
import com.expense.tracker.feature.expense.data.mapper.toEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionMapperTest {
    @Test
    fun mapsExpenseCategoryEntityToDomain() {
        val entity = TransactionEntity(
            id = "txn-1",
            amount = 42.50,
            type = "EXPENSE",
            category = "FOOD",
            note = "Lunch",
            createdAtMillis = 1_720_000_000_000,
        )

        val transaction = entity.toDomain()

        assertEquals("txn-1", transaction.id)
        assertEquals(42.50, transaction.amount)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals("FOOD", transaction.category)
        assertEquals("Lunch", transaction.note)
        assertEquals(1_720_000_000_000, transaction.createdAtMillis)
    }

    @Test
    fun mapsIncomeCategoryEntityToDomain() {
        val entity = TransactionEntity(
            id = "txn-2",
            amount = 5000.0,
            type = "INCOME",
            category = "SALARY",
            note = "Monthly salary",
            createdAtMillis = 1_720_000_000_000,
        )

        val transaction = entity.toDomain()

        assertEquals("txn-2", transaction.id)
        assertEquals(5000.0, transaction.amount)
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals("SALARY", transaction.category)
    }

    @Test
    fun mapsDomainToEntity() {
        val transaction = Transaction(
            id = "txn-3",
            amount = 100.0,
            type = TransactionType.INCOME,
            category = "SALARY",
            note = "Monthly salary",
            createdAtMillis = 1_720_000_000_000,
        )

        val entity = transaction.toEntity()

        assertEquals("txn-3", entity.id)
        assertEquals(100.0, entity.amount)
        assertEquals("INCOME", entity.type)
        assertEquals("SALARY", entity.category)
        assertEquals("Monthly salary", entity.note)
        assertEquals(1_720_000_000_000, entity.createdAtMillis)
    }

    @Test
    fun roundtripPreservesAllFields() {
        val original = Transaction(
            id = "txn-4",
            amount = 99.99,
            type = TransactionType.EXPENSE,
            category = "TRANSPORTATION",
            note = "Bus fare",
            createdAtMillis = 1_720_000_000_000,
        )

        val roundtripped = original.toEntity().toDomain()

        assertEquals(original.id, roundtripped.id)
        assertEquals(original.amount, roundtripped.amount)
        assertEquals(original.type, roundtripped.type)
        assertEquals(original.category, roundtripped.category)
        assertEquals(original.note, roundtripped.note)
        assertEquals(original.createdAtMillis, roundtripped.createdAtMillis)
    }
}
