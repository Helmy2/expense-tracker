package com.expense.tracker.feature.sample.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.expense.tracker.feature.sample.domain.model.SampleCategory
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.sample_add
import com.expense.tracker.shared.core.strings.sample_baseline_label
import com.expense.tracker.shared.core.strings.sample_cancel
import com.expense.tracker.shared.core.strings.sample_category_label
import com.expense.tracker.shared.core.strings.sample_empty_body
import com.expense.tracker.shared.core.strings.sample_empty_title
import com.expense.tracker.shared.core.strings.sample_error_title
import com.expense.tracker.shared.core.strings.sample_loading_title
import com.expense.tracker.shared.core.strings.sample_retry
import com.expense.tracker.shared.core.strings.sample_save
import com.expense.tracker.shared.core.strings.sample_subtitle
import com.expense.tracker.shared.core.strings.sample_title
import com.expense.tracker.shared.core.strings.sample_title_label
import com.expense.tracker.shared.designsystem.DreamTheme
import com.expense.tracker.shared.designsystem.components.Button
import com.expense.tracker.shared.designsystem.components.ButtonVariant
import com.expense.tracker.shared.designsystem.components.Card
import com.expense.tracker.shared.designsystem.components.CardVariant
import com.expense.tracker.shared.designsystem.components.CircularProgressIndicator
import com.expense.tracker.shared.designsystem.components.ComponentSize
import com.expense.tracker.shared.designsystem.components.ListItem
import com.expense.tracker.shared.designsystem.components.Sheet
import com.expense.tracker.shared.designsystem.components.TopAppBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleListScreen(
    viewModel: SampleViewModel = koinViewModel(),
    onNavigateDetail: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SampleEvent.NavigateToDetail -> onNavigateDetail(event.id)
                else -> {}
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(SampleAction.Load())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.sample_title),
                navigationIcon = {},
                actions = {
                    Button(
                        text = stringResource(Res.string.sample_add),
                        onClick = { viewModel.onAction(SampleAction.StartCreate) },
                        variant = ButtonVariant.Tertiary,
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = DreamTheme.spacing.lg, vertical = DreamTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.md),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm)) {
                    Text(
                        text = stringResource(Res.string.sample_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(Res.string.sample_baseline_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            when (val contentState = state.contentState) {
                SampleContentState.Loading -> item {
                    Card(
                        title = stringResource(Res.string.sample_loading_title),
                        variant = CardVariant.Filled,
                        leadingContent = {
                            CircularProgressIndicator(size = ComponentSize.Small)
                        },
                    )
                }

                SampleContentState.Empty -> item {
                    Card(
                        title = stringResource(Res.string.sample_empty_title),
                        body = stringResource(Res.string.sample_empty_body),
                        variant = CardVariant.Filled,
                        accentColor = MaterialTheme.colorScheme.secondary,
                    )
                }

                is SampleContentState.Content -> items(contentState.items) { item ->
                    Card(
                        variant = CardVariant.Elevated,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.onAction(SampleAction.SelectItem(item.id)) },
                    ) {
                        ListItem(
                            headline = item.title.asText(),
                            supportingText = item.description.asText(),
                            trailingContent = {
                                Text(
                                    text = item.category.label(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    }
                }

                is SampleContentState.Error -> item {
                    Card(
                        title = stringResource(Res.string.sample_error_title),
                        body = contentState.error.messageText(),
                        variant = CardVariant.Filled,
                        accentColor = MaterialTheme.colorScheme.error,
                        actionLabel = stringResource(Res.string.sample_retry),
                        onAction = { viewModel.onAction(SampleAction.Load(force = true)) },
                    )
                }
            }
        }
    }

    if (state.showCreateSheet) {
        Sheet(
            onDismissRequest = { viewModel.onAction(SampleAction.CancelEdit) },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.md),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(Res.string.sample_title_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

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

                Button(
                    text = stringResource(Res.string.sample_cancel),
                    onClick = { viewModel.onAction(SampleAction.CancelEdit) },
                    variant = ButtonVariant.Tertiary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
