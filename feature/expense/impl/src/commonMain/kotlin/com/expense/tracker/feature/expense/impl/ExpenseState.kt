package com.expense.tracker.feature.expense.impl

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
    val dashboard: DashboardSummaryUi = DashboardSummaryUi("$0.00", "$0.00", "$0.00"),
    val showBottomSheet: Boolean = false,
    val upcomingRecurring: List<UpcomingRecurringUi> = emptyList(),
    val deleteTargetId: String? = null,
)

sealed interface ExpenseContentState {
    data object Loading : ExpenseContentState
    data object Empty : ExpenseContentState
    data class Content(val transactions: List<ExpenseTransactionUi>) : ExpenseContentState
    data class Error(val error: AppError) : ExpenseContentState
}
