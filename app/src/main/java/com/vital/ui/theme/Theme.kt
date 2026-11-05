package com.vital.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.vital.ui.theme.VitalTypography.Typography

@Composable
private fun darkScheme(): androidx.compose.material3.ColorScheme = darkColorScheme(
    primary = VitalColors.orange,
    onPrimary = VitalColors.black,
    secondary = VitalColors.orange,
    onSecondary = VitalColors.black,
    tertiary = VitalColors.orange,
    onTertiary = VitalColors.black,
    background = VitalColors.paper,
    onBackground = VitalColors.ink,
    surface = VitalColors.paper,
    onSurface = VitalColors.ink,
    surfaceVariant = VitalColors.sheet,
    onSurfaceVariant = VitalColors.inkMuted,
    outline = VitalColors.ruleStrong,
    outlineVariant = VitalColors.rule,
    scrim = Color.Black,
    inverseSurface = VitalColors.ink,
    inverseOnSurface = VitalColors.paper,
    inversePrimary = VitalColors.orange,
    primaryContainer = VitalColors.orange,
    onPrimaryContainer = VitalColors.black,
    surfaceContainer = VitalColors.sheet,
    surfaceContainerHigh = VitalColors.sheet,
    surfaceContainerHighest = VitalColors.sheet,
    surfaceContainerLow = VitalColors.paper,
    surfaceContainerLowest = VitalColors.paper,
    surfaceBright = VitalColors.sheet,
    surfaceDim = VitalColors.paper
)

@Composable
private fun lightScheme(): androidx.compose.material3.ColorScheme = lightColorScheme(
    primary = VitalColors.orange,
    onPrimary = Color.White,
    secondary = VitalColors.orange,
    onSecondary = Color.White,
    tertiary = VitalColors.orange,
    onTertiary = Color.White,
    background = VitalColors.paper,
    onBackground = VitalColors.ink,
    surface = VitalColors.paper,
    onSurface = VitalColors.ink,
    surfaceVariant = VitalColors.sheet,
    onSurfaceVariant = VitalColors.inkMuted,
    outline = VitalColors.ruleStrong,
    outlineVariant = VitalColors.rule,
    scrim = Color.Black,
    inverseSurface = VitalColors.ink,
    inverseOnSurface = VitalColors.paper,
    inversePrimary = VitalColors.orange,
    primaryContainer = VitalColors.orange.copy(alpha = 0.18f),
    onPrimaryContainer = VitalColors.orange,
    surfaceContainer = VitalColors.sheet,
    surfaceContainerHigh = VitalColors.sheet,
    surfaceContainerHighest = VitalColors.sheet,
    surfaceContainerLow = VitalColors.paper,
    surfaceContainerLowest = VitalColors.paper,
    surfaceBright = VitalColors.sheet,
    surfaceDim = VitalColors.paper
)

// True black is ON by default. MainActivity provides the live value from Settings.
val LocalTrueBlack = staticCompositionLocalOf { true }

@Composable
fun VitalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // True black only affects the dark palette.
    trueBlack: Boolean = true,
    content: @Composable () -> Unit
) {
    // Publish the palette before anything reads a token this frame.
    applyPalette(darkTheme)
    val colorScheme = if (darkTheme) darkScheme() else lightScheme()

    val context = LocalContext.current
    SideEffect {
        val window = (context as? Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val page = if (darkTheme && trueBlack) {
                VitalColors.trueBlackBackground
            } else {
                VitalColors.paper
            }
            window.statusBarColor = page.toArgb()
            window.navigationBarColor = page.toArgb()
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun currentAccentColor(): Color = VitalColors.orange

@Composable
fun isTrueBlackEnabled(): Boolean = LocalTrueBlack.current
