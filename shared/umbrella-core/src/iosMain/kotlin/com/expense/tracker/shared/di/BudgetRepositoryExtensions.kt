package com.expense.tracker.shared.di

import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.model.BudgetDetail
import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.shared.core.domain.Result

suspend fun BudgetRepository.loadBudgetsOrThrow(): List<Budget> = safeOrThrow("BudgetRepository.loadBudgets") {
    when (val result = loadBudgets()) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun BudgetRepository.loadBudgetByIdOrThrow(id: String): Budget? = safeOrThrow("BudgetRepository.loadBudgetById") {
    when (val result = loadBudgetById(id)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun BudgetRepository.loadBudgetByCategoryOrThrow(category: ExpenseCategory): Budget? = safeOrThrow("BudgetRepository.loadBudgetByCategory") {
    when (val result = loadBudgetByCategory(category)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun BudgetRepository.createBudgetOrThrow(
    category: ExpenseCategory,
    monthlyLimit: Double,
): Budget = safeOrThrow("BudgetRepository.createBudget") {
    when (val result = createBudget(category, monthlyLimit)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun BudgetRepository.updateBudgetOrThrow(
    id: String,
    monthlyLimit: Double,
): Budget = safeOrThrow("BudgetRepository.updateBudget") {
    when (val result = updateBudget(id, monthlyLimit)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun BudgetRepository.deleteBudgetOrThrow(id: String) = safeOrThrow("BudgetRepository.deleteBudget") {
    when (val result = deleteBudget(id)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun BudgetRepository.loadBudgetsWithSpendingOrThrow(): List<BudgetWithSpending> = safeOrThrow("BudgetRepository.loadBudgetsWithSpending") {
    when (val result = loadBudgetsWithSpending()) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}

suspend fun BudgetRepository.loadBudgetDetailOrThrow(id: String): BudgetDetail? = safeOrThrow("BudgetRepository.loadBudgetDetail") {
    when (val result = loadBudgetDetail(id)) {
        is Result.Success -> result.value
        is Result.Failure -> throw RuntimeException(result.error.toString())
    }
}
