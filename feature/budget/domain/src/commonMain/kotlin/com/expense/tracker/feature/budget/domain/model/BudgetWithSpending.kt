package com.expense.tracker.feature.budget.domain.model

data class BudgetWithSpending(
    val budget: Budget,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentage: Double,
    val status: BudgetStatus,
)

enum class BudgetStatus {
    UNDER_75,
    BETWEEN_75_90,
    OVER_90,
    OVER_BUDGET,
}

fun computeBudgetStatus(percentage: Double): BudgetStatus = when {
    percentage > 1.0 -> BudgetStatus.OVER_BUDGET
    percentage >= 0.90 -> BudgetStatus.OVER_90
    percentage >= 0.75 -> BudgetStatus.BETWEEN_75_90
    else -> BudgetStatus.UNDER_75
}

fun Budget.withSpending(spentAmount: Double): BudgetWithSpending {
    val percentage = if (monthlyLimit > 0) spentAmount / monthlyLimit else 0.0
    return BudgetWithSpending(
        budget = this,
        spentAmount = spentAmount,
        remainingAmount = monthlyLimit - spentAmount,
        percentage = percentage,
        status = computeBudgetStatus(percentage),
    )
}
