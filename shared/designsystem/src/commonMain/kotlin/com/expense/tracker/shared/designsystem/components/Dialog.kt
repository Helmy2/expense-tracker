package com.expense.tracker.shared.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme

@Composable
fun Dialog(
    title: String? = null,
    text: String? = null,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = if (title != null) {
            { Text(text = title, style = MaterialTheme.typography.headlineSmall) }
        } else null,
        text = if (text != null) {
            { Text(text = text, style = MaterialTheme.typography.bodyMedium) }
        } else null,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@PreviewLightDark
@Composable
private fun DialogPreview() {
    AppTheme {
        Dialog(
            title = "Confirm",
            text = "Are you sure?",
            onDismissRequest = {},
            confirmButton = { TextButton(onClick = {}) { Text("OK") } },
            dismissButton = { TextButton(onClick = {}) { Text("Cancel") } }
        )
    }
}
