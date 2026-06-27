package com.expense.tracker.shared.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme
import com.expense.tracker.shared.designsystem.DreamTheme

@Composable
fun Banner(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    dismissActionLabel: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(DreamTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dismissActionLabel != null && onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(text = dismissActionLabel)
                }
            }
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BannerPreview() {
    AppTheme {
        Banner(
            text = "This is an important announcement that requires your attention.",
            actionLabel = "View",
            onAction = {},
            dismissActionLabel = "Dismiss",
            onDismiss = {}
        )
    }
}
