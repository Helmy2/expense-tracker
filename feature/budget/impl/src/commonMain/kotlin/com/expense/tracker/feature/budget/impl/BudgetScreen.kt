package com.expense.tracker.feature.budget.impl

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.expense.tracker.shared.core.strings.budget_created_snackbar
import com.expense.tracker.shared.core.strings.budget_deleted_snackbar
import com.expense.tracker.shared.core.strings.budget_list_title
import com.expense.tracker.shared.core.strings.budget_updated_snackbar
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val createdSnackbarText = stringResource(Res.string.budget_created_snackbar)
    val updatedSnackbarText = stringResource(Res.string.budget_updated_snackbar)
    val deletedSnackbarText = stringResource(Res.string.budget_deleted_snackbar)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BudgetEvent.BudgetSaved -> {
                    snackbarHostState.showSnackbar(createdSnackbarText)
                }
                is BudgetEvent.BudgetDeleted -> {
                    snackbarHostState.showSnackbar(deletedSnackbarText)
                }
                is BudgetEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(BudgetAction.Load)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.budget_list_title),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        BudgetContent(
            state = state,
            onAction = { viewModel.onAction(it) },
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToCreate = onNavigateToCreate,
            onNavigateToEdit = onNavigateToEdit,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
