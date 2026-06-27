package com.expense.tracker.feature.expense.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expense.tracker.feature.expense.domain.model.DashboardSummary
import com.expense.tracker.feature.expense.domain.model.Transaction
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.SystemTimeProvider
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.expense_amount_label
import com.expense.tracker.shared.core.strings.expense_amount_validation
import com.expense.tracker.shared.core.strings.expense_balance_label
import com.expense.tracker.shared.core.strings.expense_category_food
import com.expense.tracker.shared.core.strings.expense_category_healthcare
import com.expense.tracker.shared.core.strings.expense_category_label
import com.expense.tracker.shared.core.strings.expense_category_other
import com.expense.tracker.shared.core.strings.expense_category_rent
import com.expense.tracker.shared.core.strings.expense_category_salary
import com.expense.tracker.shared.core.strings.expense_category_shopping
import com.expense.tracker.shared.core.strings.expense_category_transportation
import com.expense.tracker.shared.core.strings.expense_category_education
import com.expense.tracker.shared.core.strings.expense_category_entertainment
import com.expense.tracker.shared.core.strings.expense_category_utilities
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
    val mapper = remember { ExpensePresentationMapper(SystemTimeProvider) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DreamTheme.spacing.md, vertical = DreamTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
    ) {
        when (val contentState = state.contentState) {
            is ExpenseContentState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                    onAction = onAction,
                    mapper = mapper,
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
private fun DashboardSection(dashboard: DashboardSummary) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.expense_balance_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatCurrency(dashboard.totalBalance),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = DreamTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DashboardMetric(
                label = stringResource(Res.string.expense_income_label),
                value = formatCurrency(dashboard.totalIncome),
                valueStyle = MaterialTheme.typography.titleLarge,
                valueColor = IncomeGreen,
                modifier = Modifier.weight(1f),
            )
            DashboardMetric(
                label = stringResource(Res.string.expense_expenses_label),
                value = formatCurrency(dashboard.totalExpenses),
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
    val amountError = state.amountText.isNotEmpty() &&
        (state.amountText.toDoubleOrNull() == null || (state.amountText.toDoubleOrNull() ?: 0.0) <= 0)
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
    selectedCategory: TransactionCategory,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (TransactionCategory) -> Unit,
) {
    Box {
        TextField(
            value = selectedCategory.asLabel(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Menu(
            expanded = expanded,
            onDismissRequest = onDismiss,
        ) {
            TransactionCategory.entries.forEach { category ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = category.asLabel()) },
                    onClick = { onSelect(category) },
                )
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

@Composable
private fun TransactionListSection(
    transactions: List<Transaction>,
    onAction: (ExpenseAction) -> Unit,
    mapper: ExpensePresentationMapper,
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
                onDelete = { onAction(ExpenseAction.DeleteTransaction(transaction.id)) },
                mapper = mapper,
            )
            HorizontalDivider(modifier = Modifier.padding(start = DreamTheme.spacing.md))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit,
    mapper: ExpensePresentationMapper,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val iconTint = if (isIncome) IncomeGreen else ExpenseRed
    val formattedAmount = mapper.formatAmount(transaction.amount, isIncome)
    val formattedDate = mapper.formatDate(transaction.createdAtMillis)

    var showDeleteDialog by remember { mutableStateOf(false) }

    ListItem(
        headline = formattedAmount,
        supportingText = "${transaction.category.asLabel()} \u00B7 $formattedDate",
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) IncomeGreen.copy(alpha = 0.15f)
                        else ExpenseRed.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    tint = iconTint,
                    contentDescription = null,
                )
            }
        },
        trailingContent = {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "Delete transaction",
                )
            }
        },
    )

    if (showDeleteDialog) {
        Dialog(
            title = stringResource(Res.string.expense_delete_title),
            text = stringResource(Res.string.expense_delete_confirm),
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(
                    text = stringResource(Res.string.expense_delete_confirm),
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    variant = ButtonVariant.Destructive,
                )
            },
            dismissButton = {
                Button(
                    text = stringResource(Res.string.expense_delete_dismiss),
                    onClick = { showDeleteDialog = false },
                    variant = ButtonVariant.Tertiary,
                )
            },
        )
    }
}

private fun AppError.asMessageText(): String = when (this) {
    AppError.Unknown -> "Something went wrong"
    is AppError.Message -> value
}

private fun formatCurrency(amount: Double): String {
    val sign = if (amount >= 0) "" else "-"
    val abs = kotlin.math.abs(amount)
    val whole = abs.toLong()
    val frac = ((abs - whole) * 100).toInt()
    return "$sign$${whole}.${frac.toString().padStart(2, '0')}"
}
