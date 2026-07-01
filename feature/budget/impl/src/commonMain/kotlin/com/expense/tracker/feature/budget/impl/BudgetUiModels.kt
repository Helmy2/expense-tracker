package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.budget.domain.model.BudgetStatus
import com.expense.tracker.feature.expense.domain.model.ExpenseCategory

data class BudgetWithSpendingUi(
    val id: String,
    val category: ExpenseCategory,
    val formattedSpent: String,
    val formattedLimit: String,
    val percentage: Float,
    val status: BudgetStatus,
    val isOverBudget: Boolean,
)

data class BudgetDetailTransactionUi(
    val id: String,
    val formattedAmount: String,
    val category: String,
    val formattedDate: String,
)

data class BudgetDetailUi(
    val id: String,
    val category: ExpenseCategory,
    val formattedSpent: String,
    val formattedRemaining: String,
    val percentage: Float,
    val status: BudgetStatus,
    val isOverBudget: Boolean,
    val transactions: List<BudgetDetailTransactionUi>,
)
