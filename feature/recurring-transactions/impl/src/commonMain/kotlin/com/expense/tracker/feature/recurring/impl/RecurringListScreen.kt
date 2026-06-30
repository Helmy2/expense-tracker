package com.expense.tracker.feature.recurring.impl

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.expense.tracker.shared.core.strings.recurring_deleted_snackbar
import com.expense.tracker.shared.core.strings.recurring_title
import com.expense.tracker.shared.designsystem.components.FloatingActionButton
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringListScreen(
    viewModel: RecurringListViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedSnackbarText = stringResource(Res.string.recurring_deleted_snackbar)

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

    LaunchedEffect(viewModel) {
        viewModel.onAction(RecurringListAction.Load)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.recurring_title),
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
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
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
            onNavigateToCreate = onNavigateToCreate,
            onNavigateToEdit = onNavigateToEdit,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
