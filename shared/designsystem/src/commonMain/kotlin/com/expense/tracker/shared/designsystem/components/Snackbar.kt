package com.expense.tracker.shared.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar as M3Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme

@Composable
fun Snackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    M3Snackbar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        action = {
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) {
        Text(text = message)
    }
}

@PreviewLightDark
@Composable
private fun SnackbarPreview() {
    AppTheme {
        Snackbar(
            message = "Changes saved",
            actionLabel = "Undo",
            onAction = {}
        )
    }
}
