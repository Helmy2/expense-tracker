package com.expense.tracker.shared.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme
import com.expense.tracker.shared.designsystem.DreamAccentSun
import com.expense.tracker.shared.designsystem.DreamTheme

@Composable
fun Card(
    title: String? = null,
    body: String? = null,
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Elevated,
    enabled: Boolean = true,
    accentColor: Color? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val shape = RoundedCornerShape(DreamTheme.spacing.lg)
    val cardContent: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(DreamTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm)
        ) {
            if (title != null || leadingContent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DreamTheme.spacing.sm)
                ) {
                    if (leadingContent != null) {
                        leadingContent()
                    }
                    if (title != null) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(text = actionLabel)
                }
            }
            content()
        }
    }

    when (variant) {
        CardVariant.Elevated -> {
            ElevatedCard(
                onClick = onClick ?: {},
                enabled = enabled && onClick != null,
                modifier = modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                content = cardContent
            )
        }

        CardVariant.Outlined -> {
            OutlinedCard(
                onClick = onClick ?: {},
                enabled = enabled && onClick != null,
                modifier = modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                content = cardContent
            )
        }

        CardVariant.Filled -> {
            androidx.compose.material3.Card(
                onClick = onClick ?: {},
                enabled = enabled && onClick != null,
                modifier = modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                content = cardContent
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CardElevatedPreview() {
    AppTheme {
        Card(
            title = "Getting Started",
            body = "Explore the core features of this app.",
            accentColor = DreamAccentSun
        )
    }
}

@PreviewLightDark
@Composable
private fun CardFilledPreview() {
    AppTheme {
        Card(
            title = "No Data",
            body = "There is nothing to display here yet.",
            variant = CardVariant.Filled,
            accentColor = MaterialTheme.colorScheme.secondary
        )
    }
}

@PreviewLightDark
@Composable
private fun CardWithActionPreview() {
    AppTheme {
        Card(
            title = "Something went wrong",
            body = "Please try again.",
            variant = CardVariant.Filled,
            accentColor = MaterialTheme.colorScheme.error,
            actionLabel = "Retry",
            onAction = {}
        )
    }
}
