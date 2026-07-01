package com.expense.tracker.feature.budget.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator as M3LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.expense.tracker.feature.budget.domain.model.BudgetStatus
import com.expense.tracker.feature.expense.domain.model.ExpenseCategory
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.budget_empty_body
import com.expense.tracker.shared.core.strings.budget_empty_title
import com.expense.tracker.shared.core.strings.budget_error_body
import com.expense.tracker.shared.core.strings.budget_error_title
import com.expense.tracker.shared.core.strings.budget_over_budget_warning
import com.expense.tracker.shared.core.strings.budget_retry
import com.expense.tracker.shared.designsystem.DreamTheme
import com.expense.tracker.shared.designsystem.components.Card
import com.expense.tracker.shared.designsystem.components.CardVariant
import com.expense.tracker.shared.designsystem.components.CircularProgressIndicator
import com.expense.tracker.shared.designsystem.components.ComponentSize
import com.expense.tracker.shared.designsystem.components.FloatingActionButton
import com.expense.tracker.shared.designsystem.components.IconButton

import org.jetbrains.compose.resources.stringResource

private val BudgetGreen = Color(0xFF4CAF50)
private val BudgetYellow = Color(0xFFFFC107)
private val BudgetRed = Color(0xFFEF5350)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetContent(
    state: BudgetState,
    onAction: (BudgetAction) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (val contentState = state.contentState) {
            is BudgetContentState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(size = ComponentSize.Large)
                }
            }

            is BudgetContentState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize().imePadding()
                        .padding(horizontal = DreamTheme.spacing.md, vertical = DreamTheme.spacing.sm),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Card(
                        title = stringResource(Res.string.budget_empty_title),
                        body = stringResource(Res.string.budget_empty_body),
                        variant = CardVariant.Filled,
                        accentColor = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            is BudgetContentState.Content -> {
                BudgetList(
                    budgets = contentState.budgets,
                    onAction = onAction,
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToEdit = onNavigateToEdit,
                    modifier = Modifier,
                )
            }

            is BudgetContentState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().imePadding()
                        .padding(horizontal = DreamTheme.spacing.md, vertical = DreamTheme.spacing.sm),
                ) {
                    Card(
                        title = stringResource(Res.string.budget_error_title),
                        body = stringResource(Res.string.budget_error_body),
                        variant = CardVariant.Filled,
                        accentColor = MaterialTheme.colorScheme.error,
                        actionLabel = stringResource(Res.string.budget_retry),
                        onAction = { onAction(BudgetAction.Load) },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { onNavigateToCreate() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(DreamTheme.spacing.md),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add budget",
            )
        }
    }
}

@Composable
private fun BudgetList(
    budgets: List<BudgetWithSpendingUi>,
    onAction: (BudgetAction) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = DreamTheme.spacing.md, vertical = DreamTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
    ) {
        budgets.forEachIndexed { index, budget ->
            BudgetCard(
                budget = budget,
                onClick = { onNavigateToDetail(budget.id) },
                onEdit = { onNavigateToEdit(budget.id) },
            )
            if (index < budgets.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCard(
    budget: BudgetWithSpendingUi,
    onClick: () -> Unit,
    onEdit: () -> Unit,
) {
    val statusColor = when (budget.status) {
        BudgetStatus.UNDER_75 -> BudgetGreen
        BudgetStatus.BETWEEN_75_90 -> BudgetYellow
        BudgetStatus.OVER_90 -> BudgetRed
        BudgetStatus.OVER_BUDGET -> BudgetRed
    }

    Card(
        onClick = onClick,
        variant = CardVariant.Elevated,
    ) {
        Column(
            modifier = Modifier.padding(DreamTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
        ) {
            // Row 1: Header with category name and edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            tint = statusColor,
                            contentDescription = null,
                        )
                    }
                    Text(
                        text = budget.category.asLabel(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Edit budget",
                    )
                }
            }

            // Row 2: Amounts
            Text(
                text = "${budget.formattedSpent} of ${budget.formattedLimit}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Row 3: Progress bar
            M3LinearProgressIndicator(
                progress = { budget.percentage },
                modifier = Modifier.fillMaxWidth(),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            // Row 4: Over-budget warning (conditional)
            if (budget.isOverBudget) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DreamTheme.spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(Res.string.budget_over_budget_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}


