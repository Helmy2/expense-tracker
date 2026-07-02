package com.expense.tracker.feature.expense.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.asMessageText
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.expense_amount_label
import com.expense.tracker.shared.core.strings.expense_amount_validation
import com.expense.tracker.shared.core.strings.expense_balance_label
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
import com.expense.tracker.shared.core.strings.expense_delete_body
import com.expense.tracker.shared.core.strings.expense_delete_confirm
import com.expense.tracker.shared.core.strings.expense_delete_dismiss
import com.expense.tracker.shared.core.strings.expense_delete_title
import com.expense.tracker.shared.core.strings.expense_empty_body
import com.expense.tracker.shared.core.strings.expense_empty_title
import com.expense.tracker.shared.core.strings.expense_error_title
import com.expense.tracker.shared.core.strings.expense_expenses_label
import com.expense.tracker.shared.core.strings.expense_income_label
import com.expense.tracker.shared.core.strings.expense_new_transaction
import com.expense.tracker.shared.core.strings.expense_note_label
import com.expense.tracker.shared.core.strings.expense_retry
import com.expense.tracker.shared.core.strings.expense_save
import com.expense.tracker.shared.core.strings.expense_transactions_label
import com.expense.tracker.shared.core.strings.expense_type_expense
import com.expense.tracker.shared.core.strings.expense_type_income
import com.expense.tracker.shared.core.strings.expense_type_label
import com.expense.tracker.shared.designsystem.DreamTheme
import com.expense.tracker.shared.designsystem.components.Button
import com.expense.tracker.shared.designsystem.components.ButtonVariant
import com.expense.tracker.shared.designsystem.components.Card
import com.expense.tracker.shared.designsystem.components.CardVariant
import com.expense.tracker.shared.designsystem.components.CircularProgressIndicator
import com.expense.tracker.shared.designsystem.components.ComponentSize
import com.expense.tracker.shared.designsystem.components.Dialog
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.ListItem
import com.expense.tracker.shared.designsystem.components.Menu
import com.expense.tracker.shared.designsystem.components.SegmentedButton
import com.expense.tracker.shared.designsystem.components.Sheet
import com.expense.tracker.shared.designsystem.components.TextField
import org.jetbrains.compose.resources.stringResource

private val IncomeGreen = Color(0xFF4CAF50)
private val ExpenseRed = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseContent(
    state: ExpenseState,
    onAction: (ExpenseAction) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = DreamTheme.spacing.md, vertical = DreamTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
    ) {
        when (val contentState = state.contentState) {
            is ExpenseContentState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(size = ComponentSize.Large)
                }
            }

            is ExpenseContentState.Empty -> {
                EmptyStateCard()
            }

            is ExpenseContentState.Content -> {
                DashboardSection(dashboard = state.dashboard)

                HorizontalDivider(modifier = Modifier.padding(vertical = DreamTheme.spacing.xs))

                TransactionListSection(
                    transactions = contentState.transactions,
                    deleteTargetId = state.deleteTargetId,
                    onAction = onAction,
                )
            }

            is ExpenseContentState.Error -> {
                Card(
                    title = stringResource(Res.string.expense_error_title),
                    body = contentState.error.asMessageText(),
                    variant = CardVariant.Filled,
                    accentColor = MaterialTheme.colorScheme.error,
                    actionLabel = stringResource(Res.string.expense_retry),
                    onAction = { onAction(ExpenseAction.Load) },
                )
            }
        }
    }

    if (state.showBottomSheet) {
        Sheet(
            onDismissRequest = { onAction(ExpenseAction.DismissFormSheet) },
        ) {
            FormSheetContent(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        title = stringResource(Res.string.expense_empty_title),
        body = stringResource(Res.string.expense_empty_body),
        variant = CardVariant.Filled,
        accentColor = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun DashboardSection(dashboard: DashboardSummaryUi) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.expense_balance_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = dashboard.formattedBalance,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = DreamTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DashboardMetric(
                label = stringResource(Res.string.expense_income_label),
                value = dashboard.formattedIncome,
                valueStyle = MaterialTheme.typography.titleLarge,
                valueColor = IncomeGreen,
                modifier = Modifier.weight(1f),
            )
            DashboardMetric(
                label = stringResource(Res.string.expense_expenses_label),
                value = dashboard.formattedExpenses,
                valueStyle = MaterialTheme.typography.titleLarge,
                valueColor = ExpenseRed,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardMetric(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FormSheetContent(
    state: ExpenseState,
    onAction: (ExpenseAction) -> Unit,
) {
    val amountError =
        state.amountText.isNotEmpty() && (state.amountText.toDoubleOrNull() == null || (state.amountText.toDoubleOrNull()
            ?: 0.0) <= 0)
    val isFormValid = state.amountText.toDoubleOrNull()?.let { it > 0 } == true

    Column(
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(Res.string.expense_new_transaction),
            style = MaterialTheme.typography.titleMedium,
        )

        AmountField(
            value = state.amountText,
            onValueChange = { onAction(ExpenseAction.AmountChanged(it)) },
            isError = amountError,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = stringResource(Res.string.expense_type_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val typeOptions = listOf(
            stringResource(Res.string.expense_type_expense),
            stringResource(Res.string.expense_type_income),
        )
        val typeIndex = when (state.selectedType) {
            TransactionType.EXPENSE -> 0
            TransactionType.INCOME -> 1
        }
        SegmentedButton(
            selectedIndex = typeIndex,
            onOptionSelect = { index ->
                val type = when (index) {
                    0 -> TransactionType.EXPENSE
                    1 -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
                onAction(ExpenseAction.TypeSelected(type))
            },
            options = typeOptions,
            modifier = Modifier.fillMaxWidth(),
        )

        CategoryDropdown(
            selectedCategory = state.selectedCategory,
            availableCategories = state.availableCategories(),
            expanded = state.categoryMenuExpanded,
            onToggle = { onAction(ExpenseAction.ToggleCategoryMenu) },
            onDismiss = { onAction(ExpenseAction.DismissCategoryMenu) },
            onSelect = { onAction(ExpenseAction.CategorySelected(it)) },
        )

        NoteField(
            value = state.noteText,
            onValueChange = { onAction(ExpenseAction.NoteChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            text = stringResource(Res.string.expense_save),
            onClick = { onAction(ExpenseAction.SaveTransaction) },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(Res.string.expense_amount_label),
        isError = isError,
        supportingText = if (isError) stringResource(Res.string.expense_amount_validation) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

@Composable
private fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = stringResource(Res.string.expense_note_label),
        singleLine = true,
        modifier = modifier,
    )
}

@Composable
private fun CategoryDropdown(
    selectedCategory: String,
    availableCategories: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    BoxWithConstraints {
        TextField(
            value = selectedCategory.asCategoryLabel(),
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
            modifier = Modifier.fillMaxWidth()
        )

        // Transparent overlay to make the entire read-only field clickable
        Box(
            modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
        )

        Menu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.width(maxWidth),
        ) {
            availableCategories.forEach { category ->
                Box(
                    modifier = Modifier.fillMaxWidth().clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(category) },
                    ).padding(vertical = DreamTheme.spacing.sm),
                ) {
                    Text(
                        text = category.asCategoryLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun String.asCategoryLabel(): String {
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

@Composable
private fun TransactionListSection(
    transactions: List<ExpenseTransactionUi>,
    deleteTargetId: String?,
    onAction: (ExpenseAction) -> Unit,
) {
    Column {
        Text(
            text = stringResource(Res.string.expense_transactions_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = DreamTheme.spacing.md),
        )

        HorizontalDivider()

        transactions.forEach { transaction ->
            TransactionItem(
                transaction = transaction,
                deleteTargetId = deleteTargetId,
                onAction = onAction,
            )
            HorizontalDivider(modifier = Modifier.padding(start = DreamTheme.spacing.md))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionItem(
    transaction: ExpenseTransactionUi,
    deleteTargetId: String?,
    onAction: (ExpenseAction) -> Unit,
) {
    val iconTint = if (transaction.isIncome) IncomeGreen else ExpenseRed

    ListItem(
        headline = transaction.formattedAmount,
        supportingText = "${transaction.category.asCategoryLabel()} \u00B7 ${transaction.formattedDate}",
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(
                    if (transaction.isIncome) IncomeGreen.copy(alpha = 0.15f)
                    else ExpenseRed.copy(alpha = 0.15f)
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (transaction.isIncome) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    tint = iconTint,
                    contentDescription = null,
                )
            }
        },
        trailingContent = {
            IconButton(onClick = { onAction(ExpenseAction.DeleteTransaction(transaction.id)) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Delete transaction",
                )
            }
        },
    )

    if (deleteTargetId == transaction.id) {
        Dialog(
            title = stringResource(Res.string.expense_delete_title),
            text = stringResource(Res.string.expense_delete_body),
            onDismissRequest = { onAction(ExpenseAction.CancelDelete) },
            confirmButton = {
                Button(
                    text = stringResource(Res.string.expense_delete_confirm),
                    onClick = { onAction(ExpenseAction.ConfirmDelete) },
                    variant = ButtonVariant.Destructive,
                )
            },
            dismissButton = {
                Button(
                    text = stringResource(Res.string.expense_delete_dismiss),
                    onClick = { onAction(ExpenseAction.CancelDelete) },
                    variant = ButtonVariant.Tertiary,
                )
            },
        )
    }
}


