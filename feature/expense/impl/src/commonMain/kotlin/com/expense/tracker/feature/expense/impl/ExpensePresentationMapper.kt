package com.expense.tracker.feature.expense.impl

import com.expense.tracker.feature.expense.domain.model.DashboardSummary
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.domain.TimeProvider

class ExpensePresentationMapper(
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

    fun formatSignedAmount(amount: Double, isIncome: Boolean): String {
        val prefix = if (isIncome) "+" else "-"
        val abs = kotlin.math.abs(amount)
        val whole = abs.toLong()
        val frac = ((abs - whole) * 100).toInt()
        return "$prefix $${whole}.${frac.toString().padStart(2, '0')}"
    }

    fun toTransactionUi(transaction: Transaction): ExpenseTransactionUi = ExpenseTransactionUi(
        id = transaction.id,
        formattedAmount = formatSignedAmount(transaction.amount, transaction.type == TransactionType.INCOME),
        category = transaction.category,
        formattedDate = formatDate(transaction.createdAtMillis),
        isIncome = transaction.type == TransactionType.INCOME,
    )

    fun toDashboardUi(summary: DashboardSummary): DashboardSummaryUi = DashboardSummaryUi(
        formattedBalance = formatAmount(summary.totalBalance),
        formattedIncome = formatAmount(summary.totalIncome),
        formattedExpenses = formatAmount(summary.totalExpenses),
    )
}
