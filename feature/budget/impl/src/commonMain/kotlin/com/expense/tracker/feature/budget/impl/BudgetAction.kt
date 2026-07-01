package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.expense.domain.model.ExpenseCategory

sealed interface BudgetAction {
    data object Load : BudgetAction
    data class SetBudget(val budgetId: String?) : BudgetAction
    data class CategorySelected(val category: ExpenseCategory) : BudgetAction
    data object ToggleCategoryMenu : BudgetAction
    data object DismissCategoryMenu : BudgetAction
    data class LimitChanged(val value: String) : BudgetAction
    data object SaveBudget : BudgetAction
    data class DeleteBudget(val id: String) : BudgetAction
    data object ConfirmDelete : BudgetAction
    data object CancelDelete : BudgetAction
}
