package com.expense.tracker.feature.recurring.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.asMessageText
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
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
import com.expense.tracker.shared.core.strings.recurring_delete_body
import com.expense.tracker.shared.core.strings.recurring_delete_confirm
import com.expense.tracker.shared.core.strings.recurring_delete_dismiss
import com.expense.tracker.shared.core.strings.recurring_delete_title
import com.expense.tracker.shared.core.strings.recurring_empty_body
import com.expense.tracker.shared.core.strings.recurring_empty_title
import com.expense.tracker.shared.core.strings.recurring_error_title
import com.expense.tracker.shared.core.strings.recurring_next_due
import com.expense.tracker.shared.core.strings.recurring_paused_label
import com.expense.tracker.shared.core.strings.recurring_retry
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
import com.expense.tracker.shared.designsystem.components.Switch
import org.jetbrains.compose.resources.stringResource

private val IncomeGreen = Color(0xFF4CAF50)
private val ExpenseRed = Color(0xFFEF5350)

@Composable
fun RecurringListContent(
    state: RecurringListState,
    onAction: (RecurringListAction) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        when (val contentState = state.contentState) {
            is RecurringListContentState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(size = ComponentSize.Large)
                }
            }

            is RecurringListContentState.Empty -> {
                EmptyListCard(
                    onCreateClick = onNavigateToCreate,
                    modifier = Modifier.weight(1f).padding(
                        horizontal = DreamTheme.spacing.md,
                        vertical = DreamTheme.spacing.xl,
                    ),
                )
            }

            is RecurringListContentState.Content -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        items = contentState.templates,
                        key = { it.id },
                    ) { template ->
                        RecurringTemplateItem(
                            template = template,
                            onTogglePause = { onAction(RecurringListAction.TogglePause(template.id)) },
                            onDeleteClick = { onAction(RecurringListAction.ShowDeleteDialog(template.id)) },
                            onItemClick = { onNavigateToEdit(template.id) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = DreamTheme.spacing.md))
                    }
                }
            }

            is RecurringListContentState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(DreamTheme.spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        title = stringResource(Res.string.recurring_error_title),
                        body = contentState.error.asMessageText(),
                        variant = CardVariant.Filled,
                        accentColor = MaterialTheme.colorScheme.error,
                        actionLabel = stringResource(Res.string.recurring_retry),
                        onAction = { onAction(RecurringListAction.Load) },
                    )
                }
            }
        }
    }

    if (state.deleteDialogTemplateId != null) {
        Dialog(
            title = stringResource(Res.string.recurring_delete_title),
            text = stringResource(Res.string.recurring_delete_body),
            onDismissRequest = { onAction(RecurringListAction.DismissDeleteDialog) },
            confirmButton = {
                Button(
                    text = stringResource(Res.string.recurring_delete_confirm),
                    onClick = { onAction(RecurringListAction.ConfirmDelete) },
                    variant = ButtonVariant.Destructive,
                )
            },
            dismissButton = {
                Button(
                    text = stringResource(Res.string.recurring_delete_dismiss),
                    onClick = { onAction(RecurringListAction.DismissDeleteDialog) },
                    variant = ButtonVariant.Tertiary,
                )
            },
        )
    }
}

@Composable
private fun EmptyListCard(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            title = stringResource(Res.string.recurring_empty_title),
            body = stringResource(Res.string.recurring_empty_body),
            variant = CardVariant.Filled,
            accentColor = MaterialTheme.colorScheme.secondary,
            actionLabel = stringResource(Res.string.recurring_retry),
            onAction = onCreateClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringTemplateItem(
    template: RecurringTemplateUi,
    onTogglePause: () -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit,
) {
    val itemAlpha = if (template.isPaused) 0.5f else 1f

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.alpha(itemAlpha),
    ) {
        ListItem(
            headline = template.formattedAmount,
            supportingText = "${template.frequencyLabel} \u00B7 ${template.category.asLabel()}",
            leadingContent = {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(
                        if (template.isIncome) IncomeGreen.copy(alpha = 0.15f)
                        else ExpenseRed.copy(alpha = 0.15f)
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (template.isIncome) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        tint = if (template.isIncome) IncomeGreen else ExpenseRed,
                        contentDescription = null,
                    )
                }
            },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.xs),
                ) {
                    Text(
                        text = "${stringResource(Res.string.recurring_next_due)}: ${template.nextDueDateFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (template.isPaused) {
                        Text(
                            text = stringResource(Res.string.recurring_paused_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = !template.isPaused,
                        onCheckedChange = { onTogglePause() },
                    )
                }
            },
            onClick = onItemClick,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(
                start = DreamTheme.spacing.md,
                end = DreamTheme.spacing.sm,
                bottom = DreamTheme.spacing.xs,
            ),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                )
            }
        }
    }

    if (showDeleteDialog) {
        Dialog(
            title = stringResource(Res.string.recurring_delete_title),
            text = stringResource(Res.string.recurring_delete_body),
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(
                    text = stringResource(Res.string.recurring_delete_confirm),
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    variant = ButtonVariant.Destructive,
                )
            },
            dismissButton = {
                Button(
                    text = stringResource(Res.string.recurring_delete_dismiss),
                    onClick = { showDeleteDialog = false },
                    variant = ButtonVariant.Tertiary,
                )
            },
        )
    }
}

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
