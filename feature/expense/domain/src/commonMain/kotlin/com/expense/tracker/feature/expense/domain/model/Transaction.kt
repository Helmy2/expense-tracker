package com.expense.tracker.feature.expense.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val note: String,
    val createdAtMillis: Long,
)

enum class TransactionType {
    INCOME,
    EXPENSE,
}

enum class TransactionCategory {
    FOOD,
    RENT,
    SALARY,
    ENTERTAINMENT,
    TRANSPORTATION,
    UTILITIES,
    SHOPPING,
    HEALTHCARE,
    EDUCATION,
    OTHER,
}
