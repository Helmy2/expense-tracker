package com.expense.tracker.feature.expense.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val note: String,
    val createdAtMillis: Long,
)

enum class TransactionType {
    INCOME,
    EXPENSE,
}

enum class IncomeCategory {
    SALARY,
    FREELANCE,
    INVESTMENT,
    BUSINESS,
    RENTAL,
    GIFT,
    REFUND,
    OTHER_INCOME,
}

enum class ExpenseCategory {
    FOOD,
    RENT,
    ENTERTAINMENT,
    TRANSPORTATION,
    UTILITIES,
    SHOPPING,
    HEALTHCARE,
    EDUCATION,
    BILLS,
    OTHER_EXPENSE,
}

fun resolveIncomeCategory(name: String): IncomeCategory =
    IncomeCategory.valueOf(name)

fun resolveExpenseCategory(name: String): ExpenseCategory =
    ExpenseCategory.valueOf(name)

fun incomeCategories(): List<IncomeCategory> = IncomeCategory.entries

fun expenseCategories(): List<ExpenseCategory> = ExpenseCategory.entries
