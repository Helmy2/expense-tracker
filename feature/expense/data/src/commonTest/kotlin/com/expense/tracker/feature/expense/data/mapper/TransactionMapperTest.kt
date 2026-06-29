package com.expense.tracker.feature.expense.data.mapper

import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.data.entity.TransactionEntity
import com.expense.tracker.feature.expense.data.mapper.toDomain
import com.expense.tracker.feature.expense.data.mapper.toEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionMapperTest {
    @Test
    fun mapsEntityToDomain() {
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
        assertEquals(TransactionCategory.FOOD, transaction.category)
        assertEquals("Lunch", transaction.note)
        assertEquals(1_720_000_000_000, transaction.createdAtMillis)
    }

    @Test
    fun mapsDomainToEntity() {
        val transaction = Transaction(
            id = "txn-2",
            amount = 100.0,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            note = "Monthly salary",
            createdAtMillis = 1_720_000_000_000,
        )

        val entity = transaction.toEntity()

        assertEquals("txn-2", entity.id)
        assertEquals(100.0, entity.amount)
        assertEquals("INCOME", entity.type)
        assertEquals("SALARY", entity.category)
        assertEquals("Monthly salary", entity.note)
        assertEquals(1_720_000_000_000, entity.createdAtMillis)
    }

    @Test
    fun roundtripPreservesAllFields() {
        val original = Transaction(
            id = "txn-3",
            amount = 99.99,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.TRANSPORTATION,
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
