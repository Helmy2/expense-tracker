package com.expense.tracker.shared.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme
import com.expense.tracker.shared.designsystem.DreamTheme

@Composable
fun Menu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    items: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = DreamTheme.spacing.md,
                vertical = DreamTheme.spacing.xs,
            ),
            content = items,
        )
    }
}

@PreviewLightDark
@Composable
private fun MenuPreview() {
    AppTheme {
        Menu(expanded = true, onDismissRequest = {}) {
            Text("Item 1")
        }
    }
}
