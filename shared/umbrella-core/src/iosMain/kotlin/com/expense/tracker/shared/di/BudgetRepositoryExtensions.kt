package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.shared.core.domain.Result

suspend fun BudgetRepository.loadBudgetsOrThrow(): List<Budget> = when (val result = loadBudgets()) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun BudgetRepository.loadBudgetByIdOrThrow(id: String): Budget? = when (val result = loadBudgetById(id)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun BudgetRepository.loadBudgetByCategoryOrThrow(category: TransactionCategory): Budget? = when (val result = loadBudgetByCategory(category)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun BudgetRepository.createBudgetOrThrow(
    category: TransactionCategory,
    monthlyLimit: Double,
): Budget = when (val result = createBudget(category, monthlyLimit)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun BudgetRepository.updateBudgetOrThrow(
    id: String,
    monthlyLimit: Double,
): Budget = when (val result = updateBudget(id, monthlyLimit)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}

suspend fun BudgetRepository.deleteBudgetOrThrow(id: String) = when (val result = deleteBudget(id)) {
    is Result.Success -> result.value
    is Result.Failure -> throw RuntimeException(result.error.toString())
}
