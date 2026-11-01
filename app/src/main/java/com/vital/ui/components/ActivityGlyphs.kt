package com.vital.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The five activity glyphs of the Summary knob (steps, walk, run, bike, moto).
 *
 * GENERATED from Notes/vital-ui/src/icons.js and knob.js by a script. Do NOT hand-edit and do NOT retype the path
 * strings. Every glyph lives in a 24 x 24 box (same as icons.js). `steps` is the two footprints from knob.js, which
 * knob.js draws as rounded rects; they are converted to a 24-box path so that all five glyphs are drawn the same way.
 *
 * Geometry (all in design units "du" of the 393 x 855 frame, from knob.js):
 *   knob centre (87.85, 295.1), glyph centre at ICON_R_DU = 123.4 from the knob centre at `iconAngleDeg`,
 *   glyph size = 24 * ICON_SCALE = 15.84 du. Draw each glyph with: translate(centre) -> scale(ICON_SCALE, pivot = Offset.Zero)
 *   -> translate(-12, -12), then fill with a Theme.kt colour. No colour is stored here (fill is a dummy Color.Black).
 */
object ActivityGlyphs {
    const val KNOB_X_DU = 87.85f
    const val KNOB_Y_DU = 295.1f
    const val ICON_R_DU = 123.4f
    const val ICON_SCALE = 0.66f       // du per glyph unit
    const val ARC_R_DU = 92.9f         // selector arc radius
    const val SPOKE_LEN_DU = 17.9f
    const val WALK_ANGLE_DEG = 31.25f  // needle rotation is 0 at this activity

    /** Needle rotation (degrees, clockwise) for an activity: (angle - 31.25) * 0.9 -> steps -13.425, walk 0, moto 40.365. */
    fun needleRotationDeg(angleDeg: Float): Float = (angleDeg - WALK_ANGLE_DEG) * 0.9f

    class Activity(
        val id: String,
        val angleDeg: Float,      // where the spoke leaves the arc
        val iconAngleDeg: Float,  // where the glyph centre sits
        val pathData: String,     // raw SVG path data, 24 x 24 box
    ) {
        val vector: ImageVector by lazy { buildVector(id, pathData) }
        /** Compose Path in glyph units (0..24). Reuse it, do not rebuild it every frame. */
        val path: Path by lazy { PathParser().parsePathString(pathData).toPath() }
        val needleDeg: Float get() = needleRotationDeg(angleDeg)

        /** Glyph centre in du. */
        fun centreDu(): Offset {
            val a = iconAngleDeg * PI.toFloat() / 180f
            return Offset(KNOB_X_DU + ICON_R_DU * cos(a), KNOB_Y_DU + ICON_R_DU * sin(a))
        }
    }

    private fun buildVector(name: String, pathData: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
        }.build()

    private const val STEPS_D =
        "M6.123,8.681L6.123,8.681A3.939,3.939 0 0 1 10.327,12.336L10.454,14.15A3.939,3.939 0 0 1 6.799,18.355L6.799,18.355A3.939,3.939 0 0 1 2.595,14.7L2.468,12.886A3.939,3.939 0 0 1 6.123,8.681ZM5.805,19.336L7.921,19.188A1.97,1.97 0 0 1 10.023,21.015L10.023,21.015A1.97,1.97 0 0 1 8.195,23.117L6.079,23.265A1.97,1.97 0 0 1 3.977,21.438L3.977,21.438A1.97,1.97 0 0 1 5.805,19.336ZM17.032,3.833L17.032,3.833A3.939,3.939 0 0 1 21.236,7.488L21.363,9.302A3.939,3.939 0 0 1 17.708,13.506L17.708,13.506A3.939,3.939 0 0 1 13.504,9.851L13.377,8.038A3.939,3.939 0 0 1 17.032,3.833ZM16.714,14.487L18.83,14.339A1.97,1.97 0 0 1 20.932,16.167L20.932,16.167A1.97,1.97 0 0 1 19.104,18.269L16.988,18.417A1.97,1.97 0 0 1 14.886,16.589L14.886,16.589A1.97,1.97 0 0 1 16.714,14.487Z"

    private const val WALK_D =
        "M13.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM9.8 8.9L7 23h2.1l1.8-8 2.1 2v6h2v-7.5l-2.1-2 .6-3C14.8 12 16.8 13 19 13v-2c-1.9 0-3.5-1-4.3-2.4l-1-1.6c-.4-.6-1-1-1.7-1-.3 0-.5.1-.8.1L6 8.3V13h2V9.6l1.8-.7"

    private const val RUN_D =
        "M13.49 5.48c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm-3.6 13.9l1-4.4 2.1 2v6h2v-7.5l-2.1-2 .6-3c1.3 1.5 3.3 2.5 5.5 2.5v-2c-1.9 0-3.5-1-4.3-2.4l-1-1.6c-.4-.6-1-1-1.7-1-.3 0-.5.1-.8.1l-5.2 2.2v4.7h2v-3.4l1.8-.7-1.6 8.1-4.9-1-.4 2 7 1.4z"

    private const val BIKE_D =
        "M15.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM5 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm0 8.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5zM10.8 10.5l2.4-2.4.8.8c1.3 1.3 3 2.1 5.1 2.1V9c-1.5 0-2.7-.6-3.6-1.5l-1.9-1.9c-.5-.4-1-.6-1.6-.6s-1.1.2-1.4.6L7.8 8.4c-.4.4-.6.9-.6 1.4 0 .6.2 1.1.6 1.4L11 14v5h2v-6.2l-2.2-2.3zM19 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm0 8.5c-1.9 0-3.5-1.6-3.5-3.5s1.6-3.5 3.5-3.5 3.5 1.6 3.5 3.5-1.6 3.5-3.5 3.5z"

    private const val MOTO_D =
        "M19.44 9.03L15.41 5H11v2h3.59l2 2H5c-2.8 0-5 2.2-5 5s2.2 5 5 5c2.46 0 4.45-1.69 4.9-4h1.65l2.77-2.77c-.21.54-.32 1.14-.32 1.77 0 2.8 2.2 5 5 5s5-2.2 5-5c0-2.65-1.97-4.77-4.56-4.97zM7.82 15C7.4 16.15 6.28 17 5 17c-1.63 0-3-1.37-3-3s1.37-3 3-3c1.28 0 2.4.85 2.82 2H5v2h2.82zM19 17c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z"

    /** In knob order, first to last along the arc. */
    val all: List<Activity> = listOf(
        Activity("steps", 16.3f, 17.4f, STEPS_D),
        Activity("walk", 31.25f, 30.7f, WALK_D),
        Activity("run", 46.2f, 45.5f, RUN_D),
        Activity("bike", 61.15f, 59.2f, BIKE_D),
        Activity("moto", 76.1f, 72.0f, MOTO_D),
    )

    val Steps: Activity get() = all[0]
    val Walk: Activity get() = all[1]
    val Run: Activity get() = all[2]
    val Bike: Activity get() = all[3]
    val Moto: Activity get() = all[4]
}