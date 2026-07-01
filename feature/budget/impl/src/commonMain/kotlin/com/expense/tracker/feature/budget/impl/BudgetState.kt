package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.shared.core.domain.AppError

data class BudgetState(
    val contentState: BudgetContentState = BudgetContentState.Loading,
    val formMode: BudgetFormMode = BudgetFormMode.Create,
    val editingBudgetId: String? = null,
    val selectedCategory: ExpenseCategory = ExpenseCategory.OTHER_EXPENSE,
    val categoryMenuExpanded: Boolean = false,
    val limitText: String = "",
    val deleteTargetId: String? = null,
)

sealed interface BudgetContentState {
    data object Loading : BudgetContentState
    data object Empty : BudgetContentState
    data class Content(val budgets: List<BudgetWithSpendingUi>) : BudgetContentState
    data class Error(val error: AppError) : BudgetContentState
}

enum class BudgetFormMode { Create, Edit }
