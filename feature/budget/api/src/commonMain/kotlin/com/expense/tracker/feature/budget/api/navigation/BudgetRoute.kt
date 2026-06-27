package com.expense.tracker.feature.budget.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object BudgetRoute : NavKey

@Serializable
data class BudgetDetailRoute(val budgetId: String) : NavKey

@Serializable
data class BudgetFormRoute(val budgetId: String? = null) : NavKey
