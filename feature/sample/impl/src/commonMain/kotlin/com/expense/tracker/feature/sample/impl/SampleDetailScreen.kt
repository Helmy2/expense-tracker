package com.expense.tracker.feature.sample.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.sample_cancel
import com.expense.tracker.shared.core.strings.sample_category_label
import com.expense.tracker.shared.core.strings.sample_detail_title
import com.expense.tracker.shared.core.strings.sample_edit
import com.expense.tracker.shared.core.strings.sample_occurred_label
import com.expense.tracker.shared.core.strings.sample_save
import com.expense.tracker.shared.designsystem.DreamTheme
import com.expense.tracker.shared.designsystem.components.Button
import com.expense.tracker.shared.designsystem.components.ButtonVariant
import com.expense.tracker.shared.designsystem.components.CircularProgressIndicator
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleDetailScreen(
    viewModel: SampleViewModel = koinViewModel(),
    selectedId: String,
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                SampleEvent.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(SampleAction.Load())
    }

    LaunchedEffect(viewModel, selectedId) {
        viewModel.onAction(SampleAction.SelectItem(selectedId, navigate = false))
    }

    val detail = state.detailState

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.sample_detail_title),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    if (detail != null) {
                        if (detail.isEditing) {
                            Button(
                                text = stringResource(Res.string.sample_cancel),
                                onClick = { viewModel.onAction(SampleAction.CancelEdit) },
                                variant = ButtonVariant.Tertiary,
                            )
                        } else {
                            Button(
                                text = stringResource(Res.string.sample_edit),
                                onClick = { viewModel.onAction(SampleAction.StartEdit) },
                                variant = ButtonVariant.Tertiary,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .padding(horizontal = DreamTheme.spacing.lg, vertical = DreamTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.md),
            ) {
                if (detail.isEditing) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.md),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TitleField(
                                formState = state.formState,
                                onAction = { viewModel.onAction(it) },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            DescriptionField(
                                formState = state.formState,
                                onAction = { viewModel.onAction(it) },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Text(
                                text = stringResource(Res.string.sample_category_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            CategorySelector(
                                category = state.formState.category,
                                onCategoryChange = { viewModel.onAction(SampleAction.CategoryChanged(it)) },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Button(
                                text = stringResource(Res.string.sample_save),
                                onClick = { viewModel.onAction(SampleAction.Save) },
                                enabled = !state.isSaving,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                } else {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.md),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = detail.item.title.asText(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Text(
                                text = detail.item.description.asText(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(Res.string.sample_category_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = detail.item.category.label(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = stringResource(Res.string.sample_occurred_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = detail.item.occurredLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
