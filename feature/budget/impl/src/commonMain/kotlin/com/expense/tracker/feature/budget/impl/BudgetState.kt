package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.shared.core.domain.AppError

data class BudgetState(
    val contentState: BudgetContentState = BudgetContentState.Loading,
    val showFormSheet: Boolean = false,
    val formMode: BudgetFormMode = BudgetFormMode.Create,
    val editingBudgetId: String? = null,
    val selectedCategory: TransactionCategory = TransactionCategory.OTHER,
    val categoryMenuExpanded: Boolean = false,
    val limitText: String = "",
    val availableCategories: List<TransactionCategory> = TransactionCategory.entries.toList(),
    val deleteTargetId: String? = null,
)

sealed interface BudgetContentState {
    data object Loading : BudgetContentState
    data object Empty : BudgetContentState
    data class Content(val budgets: List<BudgetWithSpending>) : BudgetContentState
    data class Error(val error: AppError) : BudgetContentState
}

enum class BudgetFormMode { Create, Edit }
