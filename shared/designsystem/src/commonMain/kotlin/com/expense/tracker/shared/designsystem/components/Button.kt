package com.expense.tracker.shared.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme
import com.expense.tracker.shared.designsystem.DreamTheme

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ComponentSize = ComponentSize.Medium,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val contentPadding = when (size) {
        ComponentSize.Small -> PaddingValues(horizontal = DreamTheme.spacing.sm)
        ComponentSize.Medium -> PaddingValues(horizontal = DreamTheme.spacing.md)
        ComponentSize.Large -> PaddingValues(horizontal = DreamTheme.spacing.lg)
    }

    val content: @Composable RowScope.() -> Unit = {
        if (leadingIcon != null) leadingIcon()
        Text(text = text, style = MaterialTheme.typography.labelLarge)
        if (trailingIcon != null) trailingIcon()
    }

    when (variant) {
        ButtonVariant.Primary, ButtonVariant.Secondary, ButtonVariant.Destructive -> {
            val containerColor = when (variant) {
                ButtonVariant.Primary -> MaterialTheme.colorScheme.primary
                ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
                ButtonVariant.Destructive -> MaterialTheme.colorScheme.error
            }
            val contentColor = when (variant) {
                ButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
                ButtonVariant.Secondary -> MaterialTheme.colorScheme.onSecondary
                ButtonVariant.Destructive -> MaterialTheme.colorScheme.onError
            }
            M3Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                content = content
            )
        }
        ButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                contentPadding = contentPadding,
                content = content
            )
        }
        ButtonVariant.Tertiary -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                contentPadding = contentPadding,
                content = content
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ButtonPrimaryPreview() {
    AppTheme {
        Button(
            text = "Primary",
            onClick = {},
            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) }
        )
    }
}

@PreviewLightDark
@Composable
private fun ButtonOutlinedPreview() {
    AppTheme {
        Button(
            text = "Outlined",
            onClick = {},
            variant = ButtonVariant.Outlined,
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
        )
    }
}

@PreviewLightDark
@Composable
private fun ButtonTertiaryPreview() {
    AppTheme {
        Button(
            text = "Edit",
            onClick = {},
            variant = ButtonVariant.Tertiary,
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
        )
    }
}

@PreviewLightDark
@Composable
private fun ButtonDestructivePreview() {
    AppTheme {
        Button(
            text = "Delete",
            onClick = {},
            variant = ButtonVariant.Destructive,
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
        )
    }
}

@PreviewLightDark
@Composable
private fun ButtonLargePreview() {
    AppTheme {
        Button(
            text = "Large Button",
            onClick = {},
            size = ComponentSize.Large,
            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) }
        )
    }
}
