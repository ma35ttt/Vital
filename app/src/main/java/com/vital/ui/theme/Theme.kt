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
    primary = VitalColors.AccentTDY,
    onPrimary = VitalColors.PrimaryText,
    secondary = VitalColors.AccentTDY,
    onSecondary = VitalColors.PrimaryText,
    tertiary = VitalColors.AccentTDY,
    onTertiary = VitalColors.PrimaryText,
    background = VitalColors.Background,
    onBackground = VitalColors.PrimaryText,
    surface = VitalColors.Card,
    onSurface = VitalColors.PrimaryText,
    surfaceVariant = VitalColors.NearBlackRow,
    onSurfaceVariant = VitalColors.MutedText,
    outline = VitalColors.NearBlackRowStroke,
    outlineVariant = VitalColors.NearBlackRowStroke,
    scrim = Color.Black,
    inverseSurface = VitalColors.PrimaryText,
    inverseOnSurface = VitalColors.Background,
    inversePrimary = VitalColors.AccentTDY,
    primaryContainer = VitalColors.AccentTDY,
    onPrimaryContainer = Color.Black,
    secondaryContainer = VitalColors.AccentTDY,
    onSecondaryContainer = Color.Black,
    tertiaryContainer = VitalColors.AccentTDY,
    onTertiaryContainer = Color.Black,
    surfaceContainer = VitalColors.Card,
    surfaceContainerHigh = VitalColors.NearBlackRow,
    surfaceContainerHighest = VitalColors.TagButtonFill,
    surfaceContainerLow = VitalColors.Background,
    surfaceContainerLowest = VitalColors.Background,
    surfaceBright = VitalColors.Card,
    surfaceDim = VitalColors.Background,
)

private val LightColorScheme = lightColorScheme(
    primary = VitalColors.AccentTDY,
    onPrimary = Color.White,
    secondary = VitalColors.AccentTDY,
    onSecondary = Color.White,
    tertiary = VitalColors.AccentTDY,
    onTertiary = Color.White,
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
    inversePrimary = VitalColors.AccentTDY,
    primaryContainer = VitalColors.AccentTDY.copy(alpha = 0.2f),
    onPrimaryContainer = VitalColors.AccentTDY,
    secondaryContainer = VitalColors.AccentTDY.copy(alpha = 0.2f),
    onSecondaryContainer = VitalColors.AccentTDY,
    tertiaryContainer = VitalColors.AccentTDY.copy(alpha = 0.2f),
    onTertiaryContainer = VitalColors.AccentTDY,
)

@Composable
fun VitalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // True black setting overrides background
    trueBlack: Boolean = false,
    // Accent color for current tab (used for dynamic theming)
    accentColor: Color = VitalColors.AccentTDY,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        if (trueBlack) {
            DarkColorScheme.copy(background = VitalColors.BackgroundTrueBlack)
        } else {
            DarkColorScheme
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

// Helper to get current theme's accent color
@Composable
fun currentAccentColor(): Color {
    return MaterialTheme.colorScheme.primary
}

// Helper to check if true black is enabled
@Composable
fun isTrueBlackEnabled(): Boolean {
    // This would be driven by Settings - for now return false
    return false
}