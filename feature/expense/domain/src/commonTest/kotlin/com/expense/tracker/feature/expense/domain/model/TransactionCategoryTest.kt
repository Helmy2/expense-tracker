package com.expense.tracker.feature.expense.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionCategoryTest {
    @Test
    fun allExpectedCategoriesExist() {
        val expected = setOf(
            TransactionCategory.FOOD,
            TransactionCategory.RENT,
            TransactionCategory.SALARY,
            TransactionCategory.ENTERTAINMENT,
            TransactionCategory.TRANSPORTATION,
            TransactionCategory.UTILITIES,
            TransactionCategory.SHOPPING,
            TransactionCategory.HEALTHCARE,
            TransactionCategory.EDUCATION,
            TransactionCategory.OTHER,
        )

        assertEquals(10, expected.size)
        assertEquals(expected.size, TransactionCategory.entries.size)
        TransactionCategory.entries.forEach { category ->
            assertTrue(category in expected, "Unexpected category: $category")
        }
    }
}
