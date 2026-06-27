package com.expense.tracker.shared.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator as M3CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator as M3LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.expense.tracker.shared.designsystem.AppTheme

@Composable
fun LinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    if (progress != null) {
        M3LinearProgressIndicator(
            progress = { progress },
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    } else {
        M3LinearProgressIndicator(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    size: ComponentSize = ComponentSize.Medium
) {
    val strokeWidth = when (size) {
        ComponentSize.Small -> 2.dp
        ComponentSize.Medium -> 4.dp
        ComponentSize.Large -> 6.dp
    }
    M3CircularProgressIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = strokeWidth
    )
}

@PreviewLightDark
@Composable
private fun ProgressIndicatorsPreview() {
    AppTheme {
        Column {
            LinearProgressIndicator()
            CircularProgressIndicator()
        }
    }
}
