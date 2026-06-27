package com.expense.tracker.shared.designsystem.components

import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme

@Composable
fun Badge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99,
    content: @Composable () -> Unit
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            if (count > 0) {
                androidx.compose.material3.Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(
                        text = if (count <= maxCount) count.toString() else "${maxCount}+",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        content = { content() }
    )
}

@PreviewLightDark
@Composable
private fun BadgePreview() {
    AppTheme {
        Badge(count = 3) {
            Text("Notifications")
        }
    }
}
