package com.expense.tracker.feature.budget.domain.model

import com.expense.tracker.feature.expense.domain.model.Transaction

data class BudgetDetail(
    val budgetWithSpending: BudgetWithSpending,
    val transactions: List<Transaction>,
)
