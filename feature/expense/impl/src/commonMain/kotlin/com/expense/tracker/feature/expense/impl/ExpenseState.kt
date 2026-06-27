package com.expense.tracker.feature.expense.impl

import com.expense.tracker.feature.expense.domain.model.DashboardSummary
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.domain.AppError

data class ExpenseState(
    val contentState: ExpenseContentState = ExpenseContentState.Loading,
    val amountText: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: TransactionCategory = TransactionCategory.OTHER,
    val noteText: String = "",
    val categoryMenuExpanded: Boolean = false,
    val dashboard: DashboardSummary = DashboardSummary(0.0, 0.0, 0.0),
    val showBottomSheet: Boolean = false,
)

sealed interface ExpenseContentState {
    data object Loading : ExpenseContentState
    data object Empty : ExpenseContentState
    data class Content(val transactions: List<Transaction>) : ExpenseContentState
    data class Error(val error: AppError) : ExpenseContentState
}
