package com.expense.tracker.feature.expense.impl

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.expense_budgets_button
import com.expense.tracker.shared.core.strings.expense_saved_snackbar
import com.expense.tracker.shared.core.strings.expense_title
import com.expense.tracker.shared.designsystem.components.Button
import com.expense.tracker.shared.designsystem.components.ButtonVariant
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedSnackbarText = stringResource(Res.string.expense_saved_snackbar)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ExpenseEvent.TransactionSaved -> {
                    snackbarHostState.showSnackbar(savedSnackbarText)
                }

                is ExpenseEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(ExpenseAction.Load)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.expense_title),
                navigationIcon = {},
                actions = {
                    Button(
                        text = stringResource(Res.string.expense_budgets_button),
                        onClick = onNavigateToBudgets,
                        variant = ButtonVariant.Tertiary,
                    )
                    IconButton(onClick = { viewModel.onAction(ExpenseAction.ToggleFormSheet) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "plus_icon_add_transaction",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        ExpenseContent(
            state = state,
            onAction = { viewModel.onAction(it) },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
