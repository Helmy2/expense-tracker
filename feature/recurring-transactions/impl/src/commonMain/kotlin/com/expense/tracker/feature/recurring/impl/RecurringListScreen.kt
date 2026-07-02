package com.expense.tracker.feature.recurring.impl

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.recurring_created_snackbar
import com.expense.tracker.shared.core.strings.recurring_deleted_snackbar
import com.expense.tracker.shared.core.strings.recurring_title
import com.expense.tracker.shared.core.strings.recurring_updated_snackbar
import com.expense.tracker.shared.designsystem.components.FloatingActionButton
import com.expense.tracker.shared.designsystem.components.Sheet
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringListScreen(
    viewModel: RecurringListViewModel = koinViewModel(),
    formViewModel: RecurringFormViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val formState by formViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedSnackbarText = stringResource(Res.string.recurring_deleted_snackbar)
    val createdSnackbarText = stringResource(Res.string.recurring_created_snackbar)
    val updatedSnackbarText = stringResource(Res.string.recurring_updated_snackbar)

    var showFormSheet by remember { mutableStateOf(false) }
    var editingTemplateId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is RecurringListEvent.TemplateDeleted -> {
                    snackbarHostState.showSnackbar(deletedSnackbarText)
                }

                is RecurringListEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        formViewModel.eventFlow.collect { event ->
            when (event) {
                is RecurringFormEvent.RecurringSaved -> {
                    showFormSheet = false
                    val snackbarText = if (editingTemplateId != null) updatedSnackbarText else createdSnackbarText
                    snackbarHostState.showSnackbar(snackbarText)
                    viewModel.onAction(RecurringListAction.Load)
                }

                is RecurringFormEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(showFormSheet, editingTemplateId) {
        if (showFormSheet) {
            formViewModel.onAction(RecurringFormAction.SetTemplate(editingTemplateId))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(RecurringListAction.Load)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.recurring_title),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingTemplateId = null; showFormSheet = true }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "add_recurring_template",
                )
            }
        },
    ) { innerPadding ->
        RecurringListContent(
            state = state,
            onAction = { viewModel.onAction(it) },
            onNavigateToCreate = { editingTemplateId = null; showFormSheet = true },
            onNavigateToEdit = { templateId -> editingTemplateId = templateId; showFormSheet = true },
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showFormSheet) {
        Sheet(
            onDismissRequest = { showFormSheet = false },
        ) {
            RecurringFormContent(
                state = formState,
                onAction = { formViewModel.onAction(it) },
                onNavigateBack = { showFormSheet = false },
                showDeleteButton = editingTemplateId != null,
            )
        }
    }
}
