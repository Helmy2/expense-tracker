package com.expense.tracker.feature.budget.impl

import androidx.compose.runtime.Composable
import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.feature.expense.domain.model.IncomeCategory
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.expense_category_bills
import com.expense.tracker.shared.core.strings.expense_category_education
import com.expense.tracker.shared.core.strings.expense_category_entertainment
import com.expense.tracker.shared.core.strings.expense_category_food
import com.expense.tracker.shared.core.strings.expense_category_healthcare
import com.expense.tracker.shared.core.strings.expense_category_other_expense
import com.expense.tracker.shared.core.strings.expense_category_rent
import com.expense.tracker.shared.core.strings.expense_category_shopping
import com.expense.tracker.shared.core.strings.expense_category_transportation
import com.expense.tracker.shared.core.strings.expense_category_utilities
import com.expense.tracker.shared.core.strings.income_category_business
import com.expense.tracker.shared.core.strings.income_category_freelance
import com.expense.tracker.shared.core.strings.income_category_gift
import com.expense.tracker.shared.core.strings.income_category_investment
import com.expense.tracker.shared.core.strings.income_category_other_income
import com.expense.tracker.shared.core.strings.income_category_refund
import com.expense.tracker.shared.core.strings.income_category_rental
import com.expense.tracker.shared.core.strings.income_category_salary
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpenseCategory.asLabel(): String = when (this) {
    ExpenseCategory.FOOD -> stringResource(Res.string.expense_category_food)
    ExpenseCategory.RENT -> stringResource(Res.string.expense_category_rent)
    ExpenseCategory.ENTERTAINMENT -> stringResource(Res.string.expense_category_entertainment)
    ExpenseCategory.TRANSPORTATION -> stringResource(Res.string.expense_category_transportation)
    ExpenseCategory.UTILITIES -> stringResource(Res.string.expense_category_utilities)
    ExpenseCategory.SHOPPING -> stringResource(Res.string.expense_category_shopping)
    ExpenseCategory.HEALTHCARE -> stringResource(Res.string.expense_category_healthcare)
    ExpenseCategory.EDUCATION -> stringResource(Res.string.expense_category_education)
    ExpenseCategory.BILLS -> stringResource(Res.string.expense_category_bills)
    ExpenseCategory.OTHER_EXPENSE -> stringResource(Res.string.expense_category_other_expense)
}

@Composable
internal fun IncomeCategory.asLabel(): String = when (this) {
    IncomeCategory.SALARY -> stringResource(Res.string.income_category_salary)
    IncomeCategory.FREELANCE -> stringResource(Res.string.income_category_freelance)
    IncomeCategory.INVESTMENT -> stringResource(Res.string.income_category_investment)
    IncomeCategory.BUSINESS -> stringResource(Res.string.income_category_business)
    IncomeCategory.RENTAL -> stringResource(Res.string.income_category_rental)
    IncomeCategory.GIFT -> stringResource(Res.string.income_category_gift)
    IncomeCategory.REFUND -> stringResource(Res.string.income_category_refund)
    IncomeCategory.OTHER_INCOME -> stringResource(Res.string.income_category_other_income)
}

/**
 * Resolves a category name string (e.g. "FOOD", "SALARY") to its localized label.
 * Matches against known IncomeCategory and ExpenseCategory names.
 */
@Composable
internal fun String.asCategoryLabel(): String {
    val categoryName = this
    return when (categoryName) {
        "SALARY" -> stringResource(Res.string.income_category_salary)
        "FREELANCE" -> stringResource(Res.string.income_category_freelance)
        "INVESTMENT" -> stringResource(Res.string.income_category_investment)
        "BUSINESS" -> stringResource(Res.string.income_category_business)
        "RENTAL" -> stringResource(Res.string.income_category_rental)
        "GIFT" -> stringResource(Res.string.income_category_gift)
        "REFUND" -> stringResource(Res.string.income_category_refund)
        "OTHER_INCOME" -> stringResource(Res.string.income_category_other_income)
        "FOOD" -> stringResource(Res.string.expense_category_food)
        "RENT" -> stringResource(Res.string.expense_category_rent)
        "ENTERTAINMENT" -> stringResource(Res.string.expense_category_entertainment)
        "TRANSPORTATION" -> stringResource(Res.string.expense_category_transportation)
        "UTILITIES" -> stringResource(Res.string.expense_category_utilities)
        "SHOPPING" -> stringResource(Res.string.expense_category_shopping)
        "HEALTHCARE" -> stringResource(Res.string.expense_category_healthcare)
        "EDUCATION" -> stringResource(Res.string.expense_category_education)
        "BILLS" -> stringResource(Res.string.expense_category_bills)
        "OTHER_EXPENSE" -> stringResource(Res.string.expense_category_other_expense)
        else -> categoryName
    }
}
