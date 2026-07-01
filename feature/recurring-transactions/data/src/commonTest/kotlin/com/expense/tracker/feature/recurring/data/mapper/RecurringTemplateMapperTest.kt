package com.expense.tracker.feature.recurring.data.mapper

import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.data.mapper.toDomain
import com.expense.tracker.feature.recurring.data.mapper.toEntity
import com.expense.tracker.shared.core.data.entity.RecurringTemplateEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecurringTemplateMapperTest {
    @Test
    fun mapsExpenseCategoryEntityToDomain() {
        val entity = RecurringTemplateEntity(
            id = "tmpl-1",
            amount = 1500.0,
            type = "EXPENSE",
            category = "RENT",
            note = "Monthly rent",
            frequency = "MONTHLY",
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        val domain = entity.toDomain()

        assertEquals("tmpl-1", domain.id)
        assertEquals(1500.0, domain.amount)
        assertEquals(TransactionType.EXPENSE, domain.type)
        assertEquals("RENT", domain.category)
        assertEquals("Monthly rent", domain.note)
        assertEquals(RecurringFrequency.MONTHLY, domain.frequency)
        assertEquals(1_720_000_000_000, domain.startDateMillis)
        assertNull(domain.endDateMillis)
        assertEquals(false, domain.isPaused)
        assertNull(domain.lastGeneratedDateMillis)
        assertEquals(1_720_000_000_000, domain.createdAtMillis)
        assertEquals(1_720_000_000_000, domain.updatedAtMillis)
    }

    @Test
    fun mapsIncomeCategoryEntityToDomain() {
        val entity = RecurringTemplateEntity(
            id = "tmpl-2",
            amount = 5000.0,
            type = "INCOME",
            category = "SALARY",
            note = "Monthly salary",
            frequency = "MONTHLY",
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = false,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        val domain = entity.toDomain()

        assertEquals("tmpl-2", domain.id)
        assertEquals(5000.0, domain.amount)
        assertEquals(TransactionType.INCOME, domain.type)
        assertEquals("SALARY", domain.category)
    }

    @Test
    fun mapsDomainToEntity() {
        val domain = RecurringTemplate(
            id = "tmpl-3",
            amount = 200.0,
            type = TransactionType.EXPENSE,
            category = "FOOD",
            note = "Weekly groceries",
            frequency = RecurringFrequency.WEEKLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = 1_780_000_000_000,
            isPaused = false,
            lastGeneratedDateMillis = 1_720_000_000_000,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        val entity = domain.toEntity()

        assertEquals("tmpl-3", entity.id)
        assertEquals(200.0, entity.amount)
        assertEquals("EXPENSE", entity.type)
        assertEquals("FOOD", entity.category)
        assertEquals("Weekly groceries", entity.note)
        assertEquals("WEEKLY", entity.frequency)
        assertEquals(1_720_000_000_000, entity.startDateMillis)
        assertEquals(1_780_000_000_000, entity.endDateMillis)
        assertEquals(false, entity.isPaused)
        assertEquals(1_720_000_000_000, entity.lastGeneratedDateMillis)
        assertEquals(1_720_000_000_000, entity.createdAtMillis)
        assertEquals(1_720_000_000_000, entity.updatedAtMillis)
    }

    @Test
    fun roundtripPreservesAllFields() {
        val original = RecurringTemplate(
            id = "tmpl-4",
            amount = 99.99,
            type = TransactionType.INCOME,
            category = "SALARY",
            note = "Monthly salary",
            frequency = RecurringFrequency.MONTHLY,
            startDateMillis = 1_720_000_000_000,
            endDateMillis = null,
            isPaused = true,
            lastGeneratedDateMillis = null,
            createdAtMillis = 1_720_000_000_000,
            updatedAtMillis = 1_720_000_000_000,
        )

        val roundtripped = original.toEntity().toDomain()

        assertEquals(original.id, roundtripped.id)
        assertEquals(original.amount, roundtripped.amount)
        assertEquals(original.type, roundtripped.type)
        assertEquals(original.category, roundtripped.category)
        assertEquals(original.note, roundtripped.note)
        assertEquals(original.frequency, roundtripped.frequency)
        assertEquals(original.startDateMillis, roundtripped.startDateMillis)
        assertEquals(original.endDateMillis, roundtripped.endDateMillis)
        assertEquals(original.isPaused, roundtripped.isPaused)
        assertEquals(original.lastGeneratedDateMillis, roundtripped.lastGeneratedDateMillis)
        assertEquals(original.createdAtMillis, roundtripped.createdAtMillis)
        assertEquals(original.updatedAtMillis, roundtripped.updatedAtMillis)
    }
}
