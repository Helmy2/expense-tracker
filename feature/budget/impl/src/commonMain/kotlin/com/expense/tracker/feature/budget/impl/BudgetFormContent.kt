package com.expense.tracker.feature.budget.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.budget_category_label
import com.expense.tracker.shared.core.strings.budget_form_create_title
import com.expense.tracker.shared.core.strings.budget_form_edit_title
import com.expense.tracker.shared.core.strings.budget_limit_label
import com.expense.tracker.shared.core.strings.budget_limit_placeholder
import com.expense.tracker.shared.core.strings.budget_limit_validation
import com.expense.tracker.shared.core.strings.budget_save
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
import com.expense.tracker.shared.designsystem.DreamTheme
import com.expense.tracker.shared.designsystem.components.Button
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.Menu
import com.expense.tracker.shared.designsystem.components.TextField
import org.jetbrains.compose.resources.stringResource

@Composable
fun BudgetFormContent(
    state: BudgetState,
    onAction: (BudgetAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEditMode = state.formMode == BudgetFormMode.Edit
    val limitValue = state.limitText.toDoubleOrNull()
    val isLimitValid = limitValue != null && limitValue > 0
    val limitError = state.limitText.isNotEmpty() && !isLimitValid
    val isFormValid = isLimitValid

    Column(
        modifier = modifier.padding(DreamTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(
                if (isEditMode) Res.string.budget_form_edit_title
                else Res.string.budget_form_create_title,
            ),
            style = MaterialTheme.typography.titleMedium,
        )

        // Category picker (only in create mode)
        if (!isEditMode) {
            CategoryDropdown(
                selectedCategory = state.selectedCategory,
                expanded = state.categoryMenuExpanded,
                onToggle = { onAction(BudgetAction.ToggleCategoryMenu) },
                onDismiss = { onAction(BudgetAction.DismissCategoryMenu) },
                onSelect = { onAction(BudgetAction.CategorySelected(it)) },
            )
        } else {
            Text(
                text = stringResource(Res.string.budget_category_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = state.selectedCategory.asLabel(),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        // Limit input
        Text(
            text = stringResource(Res.string.budget_limit_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextField(
            value = state.limitText,
            onValueChange = { onAction(BudgetAction.LimitChanged(it)) },
            placeholder = stringResource(Res.string.budget_limit_placeholder),
            isError = limitError,
            supportingText = if (limitError) {
                stringResource(Res.string.budget_limit_validation)
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        // Save button
        Button(
            text = stringResource(Res.string.budget_save),
            onClick = { onAction(BudgetAction.SaveBudget) },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: TransactionCategory,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (TransactionCategory) -> Unit,
) {
    BoxWithConstraints {
        TextField(
            value = selectedCategory.asLabel(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        // Transparent overlay to make the entire read-only field clickable
        Box(
            modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            ),
        )

        Menu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.width(maxWidth),
        ) {
            TransactionCategory.entries.forEach { category ->
                Box(
                    modifier = Modifier.fillMaxWidth().clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(category) },
                    ).padding(vertical = DreamTheme.spacing.sm),
                ) {
                    Text(
                        text = category.asLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionCategory.asLabel(): String = when (this) {
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
