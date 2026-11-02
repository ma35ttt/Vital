package com.vital.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography.Typography

private val DarkColorScheme = darkColorScheme(
    primary = VitalColors.orange,
    onPrimary = VitalColors.black,
    secondary = VitalColors.orange,
    onSecondary = VitalColors.black,
    tertiary = VitalColors.orange,
    onTertiary = VitalColors.black,
    background = VitalColors.black,
    onBackground = VitalColors.textPrimary,
    surface = VitalColors.wellFill,
    onSurface = VitalColors.textPrimary,
    surfaceVariant = VitalColors.wellFill,
    onSurfaceVariant = VitalColors.textMuted,
    outline = VitalColors.wellStroke,
    outlineVariant = VitalColors.wellStroke,
    scrim = Color.Black,
    inverseSurface = VitalColors.textPrimary,
    inverseOnSurface = VitalColors.black,
    inversePrimary = VitalColors.orange,
    primaryContainer = VitalColors.orange,
    onPrimaryContainer = VitalColors.black,
    secondaryContainer = VitalColors.orange,
    onSecondaryContainer = VitalColors.black,
    tertiaryContainer = VitalColors.orange,
    onTertiaryContainer = VitalColors.black,
    surfaceContainer = VitalColors.wellFill,
    surfaceContainerHigh = VitalColors.wellFill,
    surfaceContainerHighest = VitalColors.wellFill,
    surfaceContainerLow = VitalColors.black,
    surfaceContainerLowest = VitalColors.black,
    surfaceBright = VitalColors.wellFill,
    surfaceDim = VitalColors.black,
)

private val LightColorScheme = lightColorScheme(
    primary = VitalColors.orange,
    onPrimary = VitalColors.black,
    secondary = VitalColors.orange,
    onSecondary = VitalColors.black,
    tertiary = VitalColors.orange,
    onTertiary = VitalColors.black,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1C1C),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF6E6E6E),
    outline = Color(0xFFC0C0C0),
    outlineVariant = Color(0xFFC0C0C0),
    scrim = Color.Black,
    inverseSurface = Color(0xFF1C1C1C),
    inverseOnSurface = Color.White,
    inversePrimary = VitalColors.orange,
    primaryContainer = VitalColors.orange.copy(alpha = 0.2f),
    onPrimaryContainer = VitalColors.orange,
    secondaryContainer = VitalColors.orange.copy(alpha = 0.2f),
    onSecondaryContainer = VitalColors.orange,
    tertiaryContainer = VitalColors.orange.copy(alpha = 0.2f),
    onTertiaryContainer = VitalColors.orange,
)

@Composable
fun VitalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // True black setting overrides background
    trueBlack: Boolean = false,
    // Accent color for current tab - always orange now
    accentColor: Color = VitalColors.orange,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        if (trueBlack) {
            DarkColorScheme.copy(background = VitalColors.trueBlackBackground)
        } else {
            DarkColorScheme.copy(background = VitalColors.falseBlackBackground)
        }
    } else {
        LightColorScheme
    }

    // Apply status bar / navigation bar colors
    val context = LocalContext.current
    SideEffect {
        val window = (context as? android.app.Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val isLight = !darkTheme
            window.setStatusBarContrastEnforced(!isLight)
            window.setNavigationBarContrastEnforced(!isLight)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Helper to get current theme's accent color (always orange now)
@Composable
fun currentAccentColor(): Color {
    return VitalColors.orange
}

// Helper to check if true black is enabled
@Composable
fun isTrueBlackEnabled(): Boolean {
    // This would be driven by Settings - for now return false
    return false
}