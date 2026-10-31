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
 * Scale = screenWidthDp / 393 (the design frame width in du)
 */
data class DesignUnits(val scale: Float) {
    /**
     * Convert design units (du) to device pixels.
     */
    fun Float.toPx(): Float = this * scale

    /**
     * Convert design units to Dp (density-independent pixels).
     */
    fun Float.toDp(): Dp = (this * scale).dp

    /**
     * Create an Offset from design units.
     */
    fun offset(x: Float, y: Float): Offset = Offset(x.toPx(), y.toPx())

    /**
     * Create a Size from design units.
     */
    fun size(width: Float, height: Float): Size = Size(width.toPx(), height.toPx())
}

/**
 * CompositionLocal for providing DesignUnits to the component tree.
 * The value is calculated from the screen width in dp.
 */
val LocalDesignUnits: CompositionLocal<DesignUnits> = compositionLocalOf {
    DesignUnits(scale = 1f) // Fallback, will be overridden by DesignUnitsProvider
}

/**
 * Static CompositionLocal for accessing the scale factor without recomposition.
 * Used for static calculations that don't need to observe changes.
 */
val LocalDesignUnitsScale: CompositionLocal<Float> = staticCompositionLocalOf {
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
 * Extension on Float to convert design units to pixels using the current DesignUnits.
 */
@Composable
fun Float.du(): Float = LocalDesignUnits.current.scale * this

/**
 * Extension on Int to convert design units to pixels using the current DesignUnits.
 */
@Composable
fun Int.du(): Float = LocalDesignUnits.current.scale * this

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