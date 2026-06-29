package com.expense.tracker.feature.budget.domain.repository

import com.expense.tracker.feature.budget.domain.model.Budget
import com.expense.tracker.feature.budget.domain.model.BudgetDetail
import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.shared.core.domain.Result

interface BudgetRepository {
    suspend fun loadBudgets(): Result<List<Budget>>
    suspend fun loadBudgetById(id: String): Result<Budget?>
    suspend fun loadBudgetByCategory(category: TransactionCategory): Result<Budget?>
    suspend fun createBudget(category: TransactionCategory, monthlyLimit: Double): Result<Budget>
    suspend fun updateBudget(id: String, monthlyLimit: Double): Result<Budget>
    suspend fun deleteBudget(id: String): Result<Unit>
    suspend fun loadBudgetsWithSpending(): Result<List<BudgetWithSpending>>
    suspend fun loadBudgetDetail(id: String): Result<BudgetDetail?>
}
