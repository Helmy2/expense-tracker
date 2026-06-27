package com.expense.tracker.feature.budget.impl

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator as M3LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expense.tracker.feature.budget.domain.model.BudgetDetail
import com.expense.tracker.feature.budget.domain.model.BudgetStatus
import com.expense.tracker.feature.budget.domain.model.withSpending
import com.expense.tracker.feature.budget.domain.repository.BudgetRepository
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.expense.domain.repository.TransactionRepository
import com.expense.tracker.shared.core.domain.AppError
import com.expense.tracker.shared.core.domain.Result
import com.expense.tracker.shared.core.domain.SystemTimeProvider
import com.expense.tracker.shared.core.domain.TimeProvider
import com.expense.tracker.shared.core.domain.YearMonth
import com.expense.tracker.shared.core.presentation.MviViewModel
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.budget_delete_body
import com.expense.tracker.shared.core.strings.budget_delete_confirm
import com.expense.tracker.shared.core.strings.budget_delete_dismiss
import com.expense.tracker.shared.core.strings.budget_delete_title
import com.expense.tracker.shared.core.strings.budget_error_body
import com.expense.tracker.shared.core.strings.budget_error_title
import com.expense.tracker.shared.core.strings.budget_no_transactions_body
import com.expense.tracker.shared.core.strings.budget_no_transactions_title
import com.expense.tracker.shared.core.strings.budget_over_budget_warning
import com.expense.tracker.shared.core.strings.budget_remaining_label
import com.expense.tracker.shared.core.strings.budget_retry
import com.expense.tracker.shared.core.strings.budget_spent_label
import com.expense.tracker.shared.core.strings.budget_transactions_title
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
import com.expense.tracker.shared.designsystem.components.TopAppBar
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val BudgetGreen = Color(0xFF4CAF50)
private val BudgetYellow = Color(0xFFFFC107)
private val BudgetRed = Color(0xFFEF5350)

// ── Detail ViewModel ───────────────────────────────────────────────────

class BudgetDetailViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val timeProvider: TimeProvider,
) : MviViewModel<BudgetDetailState, BudgetDetailAction, BudgetDetailEvent>(
    initialState = BudgetDetailState(),
) {
    private var budgetId: String = ""

    override suspend fun handleAction(action: BudgetDetailAction) {
        when (action) {
            is BudgetDetailAction.Load -> {
                budgetId = action.budgetId
                load()
            }
            is BudgetDetailAction.DeleteBudget -> deleteBudget()
        }
    }

    private suspend fun load() {
        updateState { it.copy(contentState = BudgetDetailContentState.Loading) }

        when (val result = budgetRepository.loadBudgetById(budgetId)) {
            is Result.Success -> {
                val budget = result.value
                if (budget == null) {
                    updateState {
                        it.copy(contentState = BudgetDetailContentState.Error(AppError.Unknown))
                    }
                    return
                }

                val transactions = when (val txResult = transactionRepository.loadTransactions()) {
                    is Result.Success -> txResult.value
                    is Result.Failure -> emptyList()
                }

                val yearMonth = timeProvider.currentYearMonth()
                val filteredTransactions = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .filter { it.category == budget.category }
                    .filter {
                        val localDateTime = Instant.fromEpochMilliseconds(it.createdAtMillis)
                            .toLocalDateTime(timeProvider.timeZone())
                        val txYearMonth = YearMonth(
                            year = localDateTime.year,
                            month = localDateTime.monthNumber,
                        )
                        txYearMonth == yearMonth
                    }

                val spent = filteredTransactions.sumOf { it.amount }
                val budgetWithSpending = budget.withSpending(spent)

                updateState {
                    it.copy(
                        contentState = BudgetDetailContentState.Content(
                            BudgetDetail(
                                budgetWithSpending = budgetWithSpending,
                                transactions = filteredTransactions,
                            ),
                        ),
                    )
                }
            }
            is Result.Failure -> updateState {
                it.copy(contentState = BudgetDetailContentState.Error(result.error))
            }
        }
    }

    private suspend fun deleteBudget() {
        when (val result = budgetRepository.deleteBudget(budgetId)) {
            is Result.Success -> {
                sendEvent(BudgetDetailEvent.BudgetDeleted)
            }
            is Result.Failure -> {
                sendEvent(BudgetDetailEvent.Error(result.error.asMessageText()))
            }
        }
    }
}

private fun AppError.asMessageText(): String = when (this) {
    AppError.Unknown -> "Something went wrong"
    is AppError.Message -> value
}

// ── Detail State / Action / Event ──────────────────────────────────────

data class BudgetDetailState(
    val contentState: BudgetDetailContentState = BudgetDetailContentState.Loading,
)

sealed interface BudgetDetailContentState {
    data object Loading : BudgetDetailContentState
    data class Content(val detail: BudgetDetail) : BudgetDetailContentState
    data class Error(val error: AppError) : BudgetDetailContentState
}

sealed interface BudgetDetailAction {
    data class Load(val budgetId: String) : BudgetDetailAction
    data object DeleteBudget : BudgetDetailAction
}

sealed interface BudgetDetailEvent {
    data object BudgetDeleted : BudgetDetailEvent
    data class Error(val message: String) : BudgetDetailEvent
}

// ── Detail Screen ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    budgetId: String,
    viewModel: BudgetDetailViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val mapper = remember { BudgetPresentationMapper(SystemTimeProvider) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BudgetDetailEvent.BudgetDeleted -> {
                    onNavigateBack()
                }
                is BudgetDetailEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(BudgetDetailAction.Load(budgetId))
    }

    val detail = when (val cs = state.contentState) {
        is BudgetDetailContentState.Content -> cs.detail
        else -> null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = detail?.budgetWithSpending?.budget?.category?.name ?: "",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (detail != null) {
                        IconButton(onClick = { onNavigateToEdit(detail.budgetWithSpending.budget.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit budget",
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = "Delete budget",
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        BudgetDetailBody(
            state = state,
            mapper = mapper,
            onRetry = { viewModel.onAction(BudgetDetailAction.Load(budgetId)) },
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showDeleteDialog) {
        Dialog(
            title = stringResource(Res.string.budget_delete_title),
            text = stringResource(Res.string.budget_delete_body),
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(
                    text = stringResource(Res.string.budget_delete_confirm),
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onAction(BudgetDetailAction.DeleteBudget)
                    },
                    variant = ButtonVariant.Destructive,
                )
            },
            dismissButton = {
                Button(
                    text = stringResource(Res.string.budget_delete_dismiss),
                    onClick = { showDeleteDialog = false },
                    variant = ButtonVariant.Tertiary,
                )
            },
        )
    }
}

@Composable
private fun BudgetDetailBody(
    state: BudgetDetailState,
    mapper: BudgetPresentationMapper,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val contentState = state.contentState) {
        is BudgetDetailContentState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(size = ComponentSize.Large)
            }
        }

        is BudgetDetailContentState.Content -> {
            val detail = contentState.detail
            val budgetWithSpending = detail.budgetWithSpending
            val statusColor = when (budgetWithSpending.status) {
                BudgetStatus.UNDER_75 -> BudgetGreen
                BudgetStatus.BETWEEN_75_90 -> BudgetYellow
                BudgetStatus.OVER_90 -> BudgetRed
                BudgetStatus.OVER_BUDGET -> BudgetRed
            }

            Column(
                modifier = modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = DreamTheme.spacing.md, vertical = DreamTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
            ) {
                // Summary Card
                Card(variant = CardVariant.Elevated) {
                    Column(
                        modifier = Modifier.padding(DreamTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm),
                    ) {
                        Text(
                            text = budgetWithSpending.budget.category.name,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(Res.string.budget_spent_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = mapper.formatAmount(budgetWithSpending.spentAmount),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(Res.string.budget_remaining_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = mapper.formatAmount(budgetWithSpending.remainingAmount),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (budgetWithSpending.remainingAmount < 0) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        BudgetGreen
                                    },
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        M3LinearProgressIndicator(
                            progress = { budgetWithSpending.percentage.coerceIn(0.0, 1.0).toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        if (budgetWithSpending.status == BudgetStatus.OVER_BUDGET) {
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

                // Transactions Section
                Text(
                    text = stringResource(Res.string.budget_transactions_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = DreamTheme.spacing.sm),
                )

                HorizontalDivider()

                if (detail.transactions.isEmpty()) {
                    Card(
                        title = stringResource(Res.string.budget_no_transactions_title),
                        body = stringResource(Res.string.budget_no_transactions_body),
                        variant = CardVariant.Filled,
                        accentColor = MaterialTheme.colorScheme.secondary,
                    )
                } else {
                    detail.transactions.forEach { transaction ->
                        val formattedAmount = mapper.formatAmount(transaction.amount)
                        val formattedDate = mapper.formatDate(transaction.createdAtMillis)

                        ListItem(
                            headline = formattedAmount,
                            supportingText = "${transaction.category.name} \u00B7 $formattedDate",
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                        .background(BudgetRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowUp,
                                        tint = BudgetRed,
                                        contentDescription = null,
                                    )
                                }
                            },
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = DreamTheme.spacing.md))
                    }
                }
            }
        }

        is BudgetDetailContentState.Error -> {
            Column(
                modifier = modifier.fillMaxSize().imePadding()
                    .padding(horizontal = DreamTheme.spacing.md, vertical = DreamTheme.spacing.sm),
            ) {
                Card(
                    title = stringResource(Res.string.budget_error_title),
                    body = stringResource(Res.string.budget_error_body),
                    variant = CardVariant.Filled,
                    accentColor = MaterialTheme.colorScheme.error,
                    actionLabel = stringResource(Res.string.budget_retry),
                    onAction = { onRetry() },
                )
            }
        }
    }
}
