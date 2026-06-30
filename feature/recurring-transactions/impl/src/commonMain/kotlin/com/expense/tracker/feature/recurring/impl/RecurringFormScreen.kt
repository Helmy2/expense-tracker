package com.expense.tracker.feature.recurring.impl

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
import com.expense.tracker.shared.core.strings.recurring_created_snackbar
import com.expense.tracker.shared.core.strings.recurring_form_create_title
import com.expense.tracker.shared.core.strings.recurring_form_edit_title
import com.expense.tracker.shared.core.strings.recurring_updated_snackbar
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringFormScreen(
    templateId: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: RecurringFormViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditMode = templateId != null
    val createdSnackbarText = stringResource(Res.string.recurring_created_snackbar)
    val updatedSnackbarText = stringResource(Res.string.recurring_updated_snackbar)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is RecurringFormEvent.RecurringSaved -> {
                    snackbarHostState.showSnackbar(
                        if (isEditMode) updatedSnackbarText else createdSnackbarText,
                    )
                    onNavigateBack()
                }

                is RecurringFormEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(RecurringFormAction.SetTemplate(templateId))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = stringResource(
                    if (isEditMode) Res.string.recurring_form_edit_title
                    else Res.string.recurring_form_create_title,
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
        RecurringFormContent(
            state = state,
            onAction = { viewModel.onAction(it) },
            onNavigateBack = onNavigateBack,
            showDeleteButton = isEditMode,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
