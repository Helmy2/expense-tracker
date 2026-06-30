package com.expense.tracker.feature.budget.impl

import com.expense.tracker.feature.budget.domain.model.BudgetDetail
import com.expense.tracker.feature.budget.domain.model.BudgetStatus
import com.expense.tracker.feature.budget.domain.model.BudgetWithSpending
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.shared.core.domain.TimeProvider

class BudgetPresentationMapper(
    private val timeProvider: TimeProvider,
) {
    fun formatDate(millis: Long): String = timeProvider.formatDate(millis)

    fun formatAmount(amount: Double): String {
        val prefix = if (amount < 0) "-" else ""
        val abs = kotlin.math.abs(amount)
        val whole = abs.toLong()
        val frac = ((abs - whole) * 100).toInt()
        return "$prefix$${whole}.${frac.toString().padStart(2, '0')}"
    }

    fun toBudgetWithSpendingUi(bws: BudgetWithSpending): BudgetWithSpendingUi = BudgetWithSpendingUi(
        id = bws.budget.id,
        category = bws.budget.category,
        formattedSpent = formatAmount(bws.spentAmount),
        formattedLimit = formatAmount(bws.budget.monthlyLimit),
        percentage = bws.percentage.coerceIn(0.0, 1.0).toFloat(),
        status = bws.status,
        isOverBudget = bws.status == BudgetStatus.OVER_BUDGET,
    )

    fun toBudgetDetailTransactionUi(transaction: Transaction): BudgetDetailTransactionUi = BudgetDetailTransactionUi(
        id = transaction.id,
        formattedAmount = formatAmount(transaction.amount),
        category = transaction.category,
        formattedDate = formatDate(transaction.createdAtMillis),
    )

    fun toBudgetDetailUi(detail: BudgetDetail): BudgetDetailUi = BudgetDetailUi(
        id = detail.budgetWithSpending.budget.id,
        category = detail.budgetWithSpending.budget.category,
        formattedSpent = formatAmount(detail.budgetWithSpending.spentAmount),
        formattedRemaining = formatAmount(detail.budgetWithSpending.remainingAmount),
        percentage = detail.budgetWithSpending.percentage.coerceIn(0.0, 1.0).toFloat(),
        status = detail.budgetWithSpending.status,
        isOverBudget = detail.budgetWithSpending.status == BudgetStatus.OVER_BUDGET,
        transactions = detail.transactions.map(::toBudgetDetailTransactionUi),
    )
}
