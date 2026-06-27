package com.expense.tracker.feature.budget.impl

sealed interface BudgetEvent {
    data object BudgetSaved : BudgetEvent
    data object BudgetDeleted : BudgetEvent
    data class Error(val message: String) : BudgetEvent
}
