package com.expense.tracker.feature.expense.impl

import com.expense.tracker.feature.expense.domain.model.TransactionCategory

data class ExpenseTransactionUi(
    val id: String,
    val formattedAmount: String,
    val category: TransactionCategory,
    val formattedDate: String,
    val isIncome: Boolean,
)

data class DashboardSummaryUi(
    val formattedBalance: String,
    val formattedIncome: String,
    val formattedExpenses: String,
)
