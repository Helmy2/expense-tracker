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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
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
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.asMessageText
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.expense_amount_label
import com.expense.tracker.shared.core.strings.expense_amount_validation
import com.expense.tracker.shared.core.strings.expense_balance_label
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
import com.expense.tracker.shared.core.strings.recurring_next_due
import com.expense.tracker.shared.core.strings.recurring_see_all
import com.expense.tracker.shared.core.strings.recurring_upcoming_title
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
    onNavigateToRecurringList: () -> Unit = {},
    onNavigateToRecurringEdit: (String) -> Unit = {},
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

                if (state.upcomingRecurring.isNotEmpty()) {
                    DashboardUpcomingSection(
                        upcomingItems = state.upcomingRecurring,
                        onSeeAllClick = onNavigateToRecurringList,
                        onItemClick = { onNavigateToRecurringEdit(it) },
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = DreamTheme.spacing.xs))
                }

                TransactionListSection(
                    transactions = contentState.transactions,
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
private fun DashboardUpcomingSection(
    upcomingItems: List<UpcomingRecurringUi>,
    onSeeAllClick: () -> Unit,
    onItemClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = DreamTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.recurring_upcoming_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Button(
                text = stringResource(Res.string.recurring_see_all),
                onClick = onSeeAllClick,
                variant = ButtonVariant.Tertiary,
            )
        }

        Spacer(modifier = Modifier.height(DreamTheme.spacing.sm))

        upcomingItems.take(3).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item.templateId) }
                    .padding(
                        horizontal = DreamTheme.spacing.md,
                        vertical = DreamTheme.spacing.sm,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isIncome) IncomeGreen.copy(alpha = 0.15f)
                            else ExpenseRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (item.isIncome) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        tint = if (item.isIncome) IncomeGreen else ExpenseRed,
                        contentDescription = null,
                    )
                }

                Spacer(modifier = Modifier.width(DreamTheme.spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.formattedAmount,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (item.isIncome) IncomeGreen else ExpenseRed,
                    )
                    Text(
                        text = "${item.category.asLabel()} \u00B7 ${item.frequencyLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "${stringResource(Res.string.recurring_next_due)}: ${item.nextDueDateFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (item != upcomingItems.take(3).last()) {
                HorizontalDivider(modifier = Modifier.padding(start = DreamTheme.spacing.md))
            }
        }
    }
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

@Composable
private fun TransactionListSection(
    transactions: List<ExpenseTransactionUi>,
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
                onDelete = { onAction(ExpenseAction.DeleteTransaction(transaction.id)) },
            )
            HorizontalDivider(modifier = Modifier.padding(start = DreamTheme.spacing.md))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionItem(
    transaction: ExpenseTransactionUi,
    onDelete: () -> Unit,
) {
    val iconTint = if (transaction.isIncome) IncomeGreen else ExpenseRed

    var showDeleteDialog by remember { mutableStateOf(false) }

    ListItem(
        headline = transaction.formattedAmount,
        supportingText = "${transaction.category.asLabel()} \u00B7 ${transaction.formattedDate}",
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
            text = stringResource(Res.string.expense_delete_body),
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


