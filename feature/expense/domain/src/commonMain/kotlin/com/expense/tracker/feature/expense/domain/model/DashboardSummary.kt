package com.expense.tracker.feature.expense.domain.model

data class DashboardSummary(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
)

fun computeDashboard(transactions: List<Transaction>): DashboardSummary {
    val totalIncome = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    val totalExpenses = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    return DashboardSummary(
        totalBalance = totalIncome - totalExpenses,
        totalIncome = totalIncome,
        totalExpenses = totalExpenses,
    )
}
