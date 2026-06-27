package com.expense.tracker.feature.expense.impl

import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType

sealed interface ExpenseAction {
    data object Load : ExpenseAction
    data class AmountChanged(val value: String) : ExpenseAction
    data class TypeSelected(val type: TransactionType) : ExpenseAction
    data class CategorySelected(val category: TransactionCategory) : ExpenseAction
    data class NoteChanged(val value: String) : ExpenseAction
    data object ToggleCategoryMenu : ExpenseAction
    data object DismissCategoryMenu : ExpenseAction
    data object SaveTransaction : ExpenseAction
    data class DeleteTransaction(val id: String) : ExpenseAction
    data object ConfirmDelete : ExpenseAction
    data object CancelDelete : ExpenseAction
    data object ToggleFormSheet : ExpenseAction
    data object DismissFormSheet : ExpenseAction
}
