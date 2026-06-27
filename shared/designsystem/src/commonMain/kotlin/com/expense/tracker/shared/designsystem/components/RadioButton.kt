package com.expense.tracker.shared.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton as M3RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme
import com.expense.tracker.shared.designsystem.DreamTheme

@Composable
fun RadioButtonGroup(
    options: List<String>,
    selectedValue: String,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.xs)
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = option == selectedValue,
                        onClick = { onOptionSelect(option) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = DreamTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                M3RadioButton(
                    selected = option == selectedValue,
                    onClick = null,
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = DreamTheme.spacing.sm)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun RadioButtonGroupPreview() {
    AppTheme {
        RadioButtonGroup(
            options = listOf("Option A", "Option B", "Option C"),
            selectedValue = "Option A",
            onOptionSelect = {}
        )
    }
}
