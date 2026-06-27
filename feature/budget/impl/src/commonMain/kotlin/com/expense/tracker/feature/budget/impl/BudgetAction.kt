package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.expense.domain.model.TransactionCategory

sealed interface BudgetAction {
    data object Load : BudgetAction
    data object ToggleFormSheet : BudgetAction
    data object DismissFormSheet : BudgetAction
    data class StartCreate(val availableCategories: List<TransactionCategory>) : BudgetAction
    data class StartEdit(
        val budgetId: String,
        val currentLimit: Double,
        val category: TransactionCategory,
    ) : BudgetAction
    data class CategorySelected(val category: TransactionCategory) : BudgetAction
    data object ToggleCategoryMenu : BudgetAction
    data object DismissCategoryMenu : BudgetAction
    data class LimitChanged(val value: String) : BudgetAction
    data object SaveBudget : BudgetAction
    data class DeleteBudget(val id: String) : BudgetAction
    data object ConfirmDelete : BudgetAction
    data object CancelDelete : BudgetAction
}
