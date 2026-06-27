package com.expense.tracker.shared.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = DreamPrimaryLight,
    onPrimary = DreamOnPrimaryLight,
    secondary = DreamSecondaryLight,
    onSecondary = DreamOnSecondaryLight,
    background = DreamBackgroundLight,
    surface = DreamSurfaceLight,
    surfaceVariant = DreamSurfaceVariantLight,
    onSurface = DreamOnSurfaceLight,
    onSurfaceVariant = DreamOnSurfaceVariantLight,
    outline = DreamOutlineLight,
    error = DreamErrorLight,
    onError = DreamOnErrorLight
)

private val DarkColors = darkColorScheme(
    primary = DreamPrimaryDark,
    onPrimary = DreamOnPrimaryDark,
    secondary = DreamSecondaryDark,
    onSecondary = DreamOnSecondaryDark,
    background = DreamBackgroundDark,
    surface = DreamSurfaceDark,
    surfaceVariant = DreamSurfaceVariantDark,
    onSurface = DreamOnSurfaceDark,
    onSurfaceVariant = DreamOnSurfaceVariantDark,
    outline = DreamOutlineDark,
    error = DreamErrorDark,
    onError = DreamOnErrorDark
)

data class DreamSpacing(
    val xs: androidx.compose.ui.unit.Dp = 4.dp,
    val sm: androidx.compose.ui.unit.Dp = 8.dp,
    val md: androidx.compose.ui.unit.Dp = 16.dp,
    val lg: androidx.compose.ui.unit.Dp = 24.dp,
    val xl: androidx.compose.ui.unit.Dp = 32.dp
)

val LocalDreamSpacing = staticCompositionLocalOf { DreamSpacing() }

object DreamTheme {
    val spacing: DreamSpacing
        @Composable get() = LocalDreamSpacing.current
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDreamSpacing provides DreamSpacing()) {
        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
            typography = DreamTypography,
            content = content
        )
    }
}
