package com.expense.tracker.feature.recurring.impl

import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.feature.recurring.domain.model.RecurringTemplate
import com.expense.tracker.feature.recurring.domain.model.UpcomingRecurring
import com.expense.tracker.shared.core.domain.TimeProvider
import kotlin.math.abs
import kotlin.math.round

class RecurringPresentationMapper(
    private val timeProvider: TimeProvider,
) {
    fun formatDate(millis: Long): String = timeProvider.formatDate(millis)

    fun formatAmount(amount: Double): String {
        val prefix = if (amount < 0) "-" else ""
        val absolute = abs(amount)
        val whole = absolute.toLong()
        val cents = round((absolute - whole) * 100).toInt()
        return "$prefix$${whole}.${cents.toString().padStart(2, '0')}"
    }

    fun formatSignedAmount(amount: Double, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        val absolute = abs(amount)
        val whole = absolute.toLong()
        val cents = round((absolute - whole) * 100).toInt()
        return "$sign $${whole}.${cents.toString().padStart(2, '0')}"
    }

    fun toTemplateUi(template: RecurringTemplate): RecurringTemplateUi = RecurringTemplateUi(
        id = template.id,
        formattedAmount = formatSignedAmount(template.amount, template.type == TransactionType.INCOME),
        category = template.category,
        frequencyLabel = template.frequency.toLabel(),
        nextDueDateFormatted = template.lastGeneratedDateMillis?.let { formatDate(nextDueDate(it, template.frequency)) }
            ?: formatDate(template.startDateMillis),
        isPaused = template.isPaused,
        isIncome = template.type == TransactionType.INCOME,
    )

    fun toUpcomingUi(upcoming: UpcomingRecurring): UpcomingRecurringUi = UpcomingRecurringUi(
        templateId = upcoming.templateId,
        formattedAmount = formatSignedAmount(upcoming.amount, upcoming.type == TransactionType.INCOME),
        category = upcoming.category,
        frequencyLabel = upcoming.frequency.toLabel(),
        nextDueDateFormatted = formatDate(upcoming.nextDueDateMillis),
        isIncome = upcoming.type == TransactionType.INCOME,
    )

    private fun nextDueDate(lastGeneratedMillis: Long, frequency: RecurringFrequency): Long {
        val oneDay = 86_400_000L
        return when (frequency) {
            RecurringFrequency.DAILY -> lastGeneratedMillis + oneDay
            RecurringFrequency.WEEKLY -> lastGeneratedMillis + 7 * oneDay
            RecurringFrequency.MONTHLY -> lastGeneratedMillis + 30 * oneDay
            RecurringFrequency.YEARLY -> lastGeneratedMillis + 365 * oneDay
        }
    }
}

fun RecurringFrequency.toLabel(): String = when (this) {
    RecurringFrequency.DAILY -> "Daily"
    RecurringFrequency.WEEKLY -> "Weekly"
    RecurringFrequency.MONTHLY -> "Monthly"
    RecurringFrequency.YEARLY -> "Yearly"
}
