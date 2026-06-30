package com.expense.tracker.feature.budget.impl

import androidx.compose.runtime.Composable
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.expense_category_education
import com.expense.tracker.shared.core.strings.expense_category_entertainment
import com.expense.tracker.shared.core.strings.expense_category_food
import com.expense.tracker.shared.core.strings.expense_category_healthcare
import com.expense.tracker.shared.core.strings.expense_category_other
import com.expense.tracker.shared.core.strings.expense_category_rent
import com.expense.tracker.shared.core.strings.expense_category_salary
import com.expense.tracker.shared.core.strings.expense_category_shopping
import com.expense.tracker.shared.core.strings.expense_category_transportation
import com.expense.tracker.shared.core.strings.expense_category_utilities
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TransactionCategory.asLabel(): String = when (this) {
    TransactionCategory.FOOD -> stringResource(Res.string.expense_category_food)
    TransactionCategory.RENT -> stringResource(Res.string.expense_category_rent)
    TransactionCategory.SALARY -> stringResource(Res.string.expense_category_salary)
    TransactionCategory.ENTERTAINMENT -> stringResource(Res.string.expense_category_entertainment)
    TransactionCategory.TRANSPORTATION -> stringResource(Res.string.expense_category_transportation)
    TransactionCategory.UTILITIES -> stringResource(Res.string.expense_category_utilities)
    TransactionCategory.SHOPPING -> stringResource(Res.string.expense_category_shopping)
    TransactionCategory.HEALTHCARE -> stringResource(Res.string.expense_category_healthcare)
    TransactionCategory.EDUCATION -> stringResource(Res.string.expense_category_education)
    TransactionCategory.OTHER -> stringResource(Res.string.expense_category_other)
}
