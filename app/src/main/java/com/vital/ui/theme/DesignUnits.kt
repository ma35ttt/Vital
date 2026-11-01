package com.vital.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design Units system based on a 393 x 855 design frame.
 * All reference measurements are in "du" (design units) which are CSS pixels
 * of the 393 x 855 reference frame.
 */
internal const val DESIGN_FRAME_WIDTH_DU = 393f
internal const val DESIGN_FRAME_HEIGHT_DU = 855f

/**
 * Holds the scale factor from design units to device pixels.
 * Scale = screenWidthDp / 393 (the design frame width in du).
 * To get pixels: du * scale * density.
 */
data class DesignUnits(val scale: Float) {
    /**
     * Convert design units (du) to device pixels.
     */
    fun Float.toPx(density: Float = 1f): Float = this * scale * density

    /**
     * Convert design units to Dp (density-independent pixels).
     */
    fun Float.toDp(): Dp = (this * scale).dp

    /**
     * Create an Offset from design units (in pixels).
     */
    fun offset(x: Float, y: Float, density: Float = 1f): Offset = Offset(x.toPx(density), y.toPx(density))

    /**
     * Create a Size from design units (in pixels).
     */
    fun size(width: Float, height: Float, density: Float = 1f): Size = Size(width.toPx(density), height.toPx(density))
}

/**
 * CompositionLocal for providing DesignUnits to the component tree.
 * The value is calculated from the screen width in dp.
 */
val LocalDesignUnits = compositionLocalOf {
    DesignUnits(scale = 1f) // Fallback, will be overridden by DesignUnitsProvider
}

/**
 * Static CompositionLocal for accessing the scale factor without recomposition.
 * Used for static calculations that don't need to observe changes.
 */
val LocalDesignUnitsScale = staticCompositionLocalOf {
    1f // Fallback
}

/**
 * Provides DesignUnits based on the current screen density and width.
 * Wrap your app/content with this to enable the .du extension.
 */
@Composable
fun DesignUnitsProvider(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthDp = configuration.screenWidthDp
    val scale = screenWidthDp.toFloat() / DESIGN_FRAME_WIDTH_DU
    val designUnits = DesignUnits(scale = scale)

    CompositionLocalProvider(
        LocalDesignUnits provides designUnits,
        LocalDesignUnitsScale provides scale,
        content = content
    )
}

/**
 * Extension to access DesignUnits in composables.
 */
@Composable
fun designUnits(): DesignUnits = LocalDesignUnits.current

/**
 * Extension to get the scale factor directly.
 */
@Composable
fun designUnitsScale(): Float = LocalDesignUnitsScale.current

/**
 * Extension on Float to convert design units to pixels using the current DesignUnits and density.
 */
@Composable
fun Float.du(density: Float = LocalDensity.current.density): Float = LocalDesignUnits.current.scale * this * density

/**
 * Extension on Int to convert design units to pixels using the current DesignUnits and density.
 */
@Composable
fun Int.du(density: Float = LocalDensity.current.density): Float = LocalDesignUnits.current.scale * this * density

/**
 * Extension on Float to convert design units to Dp using the current DesignUnits.
 */
@Composable
fun Float.duToDp(): Dp = (LocalDesignUnits.current.scale * this).dp

/**
 * Extension on Int to convert design units to Dp using the current DesignUnits.
 */
@Composable
fun Int.duToDp(): Dp = (LocalDesignUnits.current.scale * this).dp