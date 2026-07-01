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
import com.expense.tracker.shared.core.strings.budget_form_create_title
import com.expense.tracker.shared.core.strings.budget_form_edit_title
import com.expense.tracker.shared.core.strings.budget_updated_snackbar
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetFormScreen(
    budgetId: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: BudgetViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditMode = budgetId != null
    val createdSnackbarText = stringResource(Res.string.budget_created_snackbar)
    val updatedSnackbarText = stringResource(Res.string.budget_updated_snackbar)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BudgetEvent.BudgetSaved -> {
                    snackbarHostState.showSnackbar(
                        if (isEditMode) updatedSnackbarText else createdSnackbarText,
                    )
                    onNavigateBack()
                }

                is BudgetEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is BudgetEvent.BudgetDeleted -> {
                    onNavigateBack()
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(BudgetAction.SetBudget(budgetId))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(
                    if (isEditMode) Res.string.budget_form_edit_title
                    else Res.string.budget_form_create_title,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        BudgetFormContent(
            state = state,
            onAction = { viewModel.onAction(it) },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
