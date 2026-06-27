package com.expense.tracker.shared.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton as M3SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButton(
    selectedIndex: Int,
    onOptionSelect: (Int) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, label ->
            M3SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onOptionSelect(index) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                )
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SegmentedButtonPreview() {
    AppTheme {
        SegmentedButton(
            selectedIndex = 0,
            onOptionSelect = {},
            options = listOf("Day", "Week", "Month")
        )
    }
}
