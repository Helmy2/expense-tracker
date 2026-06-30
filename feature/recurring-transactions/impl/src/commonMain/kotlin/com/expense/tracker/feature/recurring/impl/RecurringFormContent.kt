package com.expense.tracker.feature.recurring.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.expense.tracker.feature.expense.domain.model.TransactionCategory
import com.expense.tracker.feature.expense.domain.model.TransactionType
import com.expense.tracker.feature.recurring.domain.model.RecurringFrequency
import com.expense.tracker.shared.core.strings.Res
import com.expense.tracker.shared.core.strings.expense_cancel
import com.expense.tracker.shared.core.strings.recurring_amount_label
import com.expense.tracker.shared.core.strings.recurring_amount_validation
import com.expense.tracker.shared.core.strings.recurring_category_label
import com.expense.tracker.shared.core.strings.recurring_end_date_clear
import com.expense.tracker.shared.core.strings.recurring_end_date_label
import com.expense.tracker.shared.core.strings.recurring_frequency_daily
import com.expense.tracker.shared.core.strings.recurring_frequency_label
import com.expense.tracker.shared.core.strings.recurring_frequency_monthly
import com.expense.tracker.shared.core.strings.recurring_frequency_weekly
import com.expense.tracker.shared.core.strings.recurring_frequency_yearly
import com.expense.tracker.shared.core.strings.recurring_note_label
import com.expense.tracker.shared.core.strings.recurring_delete_body
import com.expense.tracker.shared.core.strings.recurring_delete_confirm
import com.expense.tracker.shared.core.strings.recurring_delete_dismiss
import com.expense.tracker.shared.core.strings.recurring_delete_title
import com.expense.tracker.shared.core.strings.recurring_save
import com.expense.tracker.shared.core.strings.recurring_start_date_label
import com.expense.tracker.shared.core.strings.recurring_type_expense
import com.expense.tracker.shared.core.strings.recurring_type_income
import com.expense.tracker.shared.core.strings.recurring_type_label
import com.expense.tracker.shared.designsystem.DreamTheme
import com.expense.tracker.shared.designsystem.components.Button
import kotlinx.datetime.toLocalDateTime
import com.expense.tracker.shared.designsystem.components.ButtonVariant
import com.expense.tracker.shared.designsystem.components.CircularProgressIndicator
import com.expense.tracker.shared.designsystem.components.ComponentSize
import com.expense.tracker.shared.designsystem.components.Dialog
import com.expense.tracker.shared.designsystem.components.IconButton
import com.expense.tracker.shared.designsystem.components.Menu
import com.expense.tracker.shared.designsystem.components.SegmentedButton
import com.expense.tracker.shared.designsystem.components.TextField

import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringFormContent(
    state: RecurringFormState,
    onAction: (RecurringFormAction) -> Unit,
    onNavigateBack: () -> Unit,
    showDeleteButton: Boolean,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(size = ComponentSize.Large)
        }
        return
    }

    val amountError = state.amountText.isNotEmpty() &&
        (state.amountText.toDoubleOrNull() == null || (state.amountText.toDoubleOrNull() ?: 0.0) <= 0)
    val isFormValid = state.amountText.toDoubleOrNull()?.let { it > 0 } == true &&
        state.startDateMillis != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DreamTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.md),
    ) {
        // Amount field
        TextField(
            value = state.amountText,
            onValueChange = { onAction(RecurringFormAction.AmountChanged(it)) },
            placeholder = stringResource(Res.string.recurring_amount_label),
            isError = amountError,
            supportingText = if (amountError) stringResource(Res.string.recurring_amount_validation) else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        // Type selector
        Text(
            text = stringResource(Res.string.recurring_type_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val typeOptions = listOf(
            stringResource(Res.string.recurring_type_expense),
            stringResource(Res.string.recurring_type_income),
        )
        val typeIndex = when (state.selectedType) {
            TransactionType.EXPENSE -> 0
            TransactionType.INCOME -> 1
        }
        SegmentedButton(
            selectedIndex = typeIndex,
            onOptionSelect = { index ->
                val type = when (index) {
                    0 -> TransactionType.EXPENSE
                    1 -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
                onAction(RecurringFormAction.TypeSelected(type))
            },
            options = typeOptions,
            modifier = Modifier.fillMaxWidth(),
        )

        // Category dropdown
        Text(
            text = stringResource(Res.string.recurring_category_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        CategoryDropdown(
            selectedCategory = state.selectedCategory,
            expanded = state.categoryMenuExpanded,
            onToggle = { onAction(RecurringFormAction.ToggleCategoryMenu) },
            onDismiss = { onAction(RecurringFormAction.DismissCategoryMenu) },
            onSelect = { onAction(RecurringFormAction.CategorySelected(it)) },
        )

        // Note field
        TextField(
            value = state.noteText,
            onValueChange = { onAction(RecurringFormAction.NoteChanged(it)) },
            placeholder = stringResource(Res.string.recurring_note_label),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Frequency selector
        Text(
            text = stringResource(Res.string.recurring_frequency_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val frequencyOptions = listOf(
            stringResource(Res.string.recurring_frequency_daily),
            stringResource(Res.string.recurring_frequency_weekly),
            stringResource(Res.string.recurring_frequency_monthly),
            stringResource(Res.string.recurring_frequency_yearly),
        )
        val frequencyIndex = when (state.selectedFrequency) {
            RecurringFrequency.DAILY -> 0
            RecurringFrequency.WEEKLY -> 1
            RecurringFrequency.MONTHLY -> 2
            RecurringFrequency.YEARLY -> 3
        }
        SegmentedButton(
            selectedIndex = frequencyIndex,
            onOptionSelect = { index ->
                val frequency = when (index) {
                    0 -> RecurringFrequency.DAILY
                    1 -> RecurringFrequency.WEEKLY
                    2 -> RecurringFrequency.MONTHLY
                    3 -> RecurringFrequency.YEARLY
                    else -> RecurringFrequency.MONTHLY
                }
                onAction(RecurringFormAction.FrequencySelected(frequency))
            },
            options = frequencyOptions,
            modifier = Modifier.fillMaxWidth(),
        )

        // Start Date
        DateField(
            label = stringResource(Res.string.recurring_start_date_label),
            dateMillis = state.startDateMillis,
            onDateSelected = { onAction(RecurringFormAction.StartDateSelected(it)) },
            onClear = null,
        )

        // End Date
        DateField(
            label = stringResource(Res.string.recurring_end_date_label),
            dateMillis = state.endDateMillis,
            onDateSelected = { onAction(RecurringFormAction.EndDateSelected(it)) },
            onClear = { onAction(RecurringFormAction.ClearEndDate) },
        )

        Spacer(modifier = Modifier.height(DreamTheme.spacing.md))

        // Save button
        Button(
            text = stringResource(Res.string.recurring_save),
            onClick = { onAction(RecurringFormAction.Save) },
            enabled = isFormValid && !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )

        // Delete button (edit mode only)
        if (showDeleteButton) {
            var showDeleteDialog by remember { mutableStateOf(false) }

            Button(
                text = stringResource(Res.string.recurring_delete_title),
                onClick = { showDeleteDialog = true },
                variant = ButtonVariant.Destructive,
                modifier = Modifier.fillMaxWidth(),
            )

            if (showDeleteDialog) {
                com.expense.tracker.shared.designsystem.components.Dialog(
                    title = stringResource(Res.string.recurring_delete_title),
                    text = stringResource(Res.string.recurring_delete_body),
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        Button(
                            text = stringResource(Res.string.recurring_delete_confirm),
                            onClick = {
                                showDeleteDialog = false
                                onAction(RecurringFormAction.Save)
                            },
                            variant = ButtonVariant.Destructive,
                        )
                    },
                    dismissButton = {
                        Button(
                            text = stringResource(Res.string.recurring_delete_dismiss),
                            onClick = { showDeleteDialog = false },
                            variant = ButtonVariant.Tertiary,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(DreamTheme.spacing.xl))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    dateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onClear: (() -> Unit)?,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextField(
            value = if (dateMillis != null) {
                val localDateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(dateMillis)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                "${localDateTime.month.ordinal + 1}/${localDateTime.dayOfMonth}/${localDateTime.year}"
            } else {
                ""
            },
            onValueChange = {},
            readOnly = true,
            placeholder = stringResource(Res.string.recurring_start_date_label),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.weight(1f),
        )

        if (onClear != null && dateMillis != null) {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = stringResource(Res.string.recurring_end_date_clear),
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis,
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.expense_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: TransactionCategory,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (TransactionCategory) -> Unit,
) {
    BoxWithConstraints {
        TextField(
            value = selectedCategory.asLabel(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        // Transparent overlay to click the read-only field
        Box(
            modifier = Modifier.matchParentSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
        )

        Menu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.width(maxWidth),
        ) {
            TransactionCategory.entries.forEach { category ->
                Box(
                    modifier = Modifier.fillMaxWidth().clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(category) },
                    ).padding(vertical = DreamTheme.spacing.sm),
                ) {
                    Text(
                        text = category.asLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
