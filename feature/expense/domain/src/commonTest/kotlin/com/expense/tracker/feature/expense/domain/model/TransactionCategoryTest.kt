package com.expense.tracker.feature.expense.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionCategoryTest {
    @Test
    fun allExpectedIncomeCategoriesExist() {
        val expected = setOf(
            IncomeCategory.SALARY,
            IncomeCategory.FREELANCE,
            IncomeCategory.INVESTMENT,
            IncomeCategory.BUSINESS,
            IncomeCategory.RENTAL,
            IncomeCategory.GIFT,
            IncomeCategory.REFUND,
            IncomeCategory.OTHER_INCOME,
        )

        assertEquals(8, expected.size)
        assertEquals(expected.size, IncomeCategory.entries.size)
        IncomeCategory.entries.forEach { category ->
            assertTrue(category in expected, "Unexpected income category: $category")
        }
    }

    @Test
    fun allExpectedExpenseCategoriesExist() {
        val expected = setOf(
            ExpenseCategory.FOOD,
            ExpenseCategory.RENT,
            ExpenseCategory.ENTERTAINMENT,
            ExpenseCategory.TRANSPORTATION,
            ExpenseCategory.UTILITIES,
            ExpenseCategory.SHOPPING,
            ExpenseCategory.HEALTHCARE,
            ExpenseCategory.EDUCATION,
            ExpenseCategory.BILLS,
            ExpenseCategory.OTHER_EXPENSE,
        )

        assertEquals(10, expected.size)
        assertEquals(expected.size, ExpenseCategory.entries.size)
        ExpenseCategory.entries.forEach { category ->
            assertTrue(category in expected, "Unexpected expense category: $category")
        }
    }

    @Test
    fun resolveIncomeCategoryReturnsCorrectValue() {
        assertEquals(IncomeCategory.SALARY, resolveIncomeCategory("SALARY"))
        assertEquals(IncomeCategory.FREELANCE, resolveIncomeCategory("FREELANCE"))
        assertEquals(IncomeCategory.OTHER_INCOME, resolveIncomeCategory("OTHER_INCOME"))
    }

    @Test
    fun resolveExpenseCategoryReturnsCorrectValue() {
        assertEquals(ExpenseCategory.FOOD, resolveExpenseCategory("FOOD"))
        assertEquals(ExpenseCategory.RENT, resolveExpenseCategory("RENT"))
        assertEquals(ExpenseCategory.OTHER_EXPENSE, resolveExpenseCategory("OTHER_EXPENSE"))
    }

    @Test
    fun incomeCategoriesReturnsAllEntries() {
        assertEquals(IncomeCategory.entries, incomeCategories())
    }

    @Test
    fun expenseCategoriesReturnsAllEntries() {
        assertEquals(ExpenseCategory.entries, expenseCategories())
    }
}
