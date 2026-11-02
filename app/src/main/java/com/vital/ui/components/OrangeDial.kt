package com.vital.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.du
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.designUnitsScale
import com.vital.ui.components.ActivityGlyphs
import com.vital.ui.components.DialTicks
import com.vital.ui.components.DialTick
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.max

// Constants from SummaryDial.kt
private const val KNOB_X = 87.85f
private const val KNOB_Y = 295.1f
private const val KNOB_OUTER = 74.61f
private const val KNOB_TIPS = 70.3f
private const val KNOB_TEETH = 60
private const val KNOB_TOOTH_DEPTH = 1.23f
private const val KNOB_PIN = 11.31f
private const val ARC_R_DU = 92.9f
private const val SPOKE_LEN_DU = 17.9f
private const val ICON_R_DU = 123.4f
private const val ICON_SCALE = 0.66f

// FAB constants
private const val FAB_X0 = 13.50f
private const val FAB_Y0 = 745.29f
private const val FAB_S = 74.43f

// Pre-computed label positions for L, M, H
private val LABEL_POSITIONS = listOf(
    Offset(50.01f, 723.33f),
    Offset(103.01f, 737.65f),
    Offset(117.09f, 789.86f)
)

// Pre-computed label style for L/M/H
private val LABEL_STYLE = TextStyle(
    color = VitalColors.RefInk,
    fontSize = 13.2.sp,
    fontWeight = FontWeight.Medium,
    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
)

/**
 * OrangeDial — decorative dial used on Meals, Lift, Cardio tabs.
 * Reuses tick tracks, knob, FAB and activity glyphs from SummaryDial/ActivityGlyphs.
 * The L/M/H letters and knob are decorative only (no click/focus/gestures).
 *
 * Parameters:
 *   - letters: 3 strings for the period labels (e.g., ["L", "M", "H"])
 *   - highlightedIndex: which period is highlighted (0, 1, or 2)
 *   - innerLabel: label inside the inner dial (e.g., "KCAL LEFT")
 *   - innerValue: large value inside the inner dial (e.g., "1,240")
 *   - innerUnit: unit text (e.g., "KCAL")
 *   - pillText: text for the pill at bottom (e.g., "TODAY")
 *   - topPill: text for the pill at top (e.g., "MOVE")
 *   - fabGlyph: which glyph on the FAB - Plus or Camera
 */
@Composable
fun OrangeDial(
    modifier: Modifier = Modifier,
    letters: List<String> = listOf("L", "M", "H"),
    highlightedIndex: Int = 1,
    innerLabel: String = "KCAL LEFT",
    innerValue: String = "1,240",
    innerUnit: String = "KCAL",
    pillText: String = "TODAY",
    topPill: String = "MOVE",
    fabGlyph: FabGlyph = FabGlyph.Plus
) {
    val textMeasurer = rememberTextMeasurer()

    // Pre-measure all text in composable context
    val labelLayouts = letters.map { letter ->
        val isHighlighted = letters.indexOf(letter) == highlightedIndex
        val style = LABEL_STYLE.copy(
            color = if (isHighlighted) VitalColors.orange else VitalColors.RefInk,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
        )
        textMeasurer.measure(letter, style)
    }

    val innerLabelLayout = textMeasurer.measure(innerLabel, TextStyle(
        color = VitalColors.black,
        fontSize = 23.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        letterSpacing = 1.sp
    ))

    val innerValueLayout = textMeasurer.measure(innerValue, TextStyle(
        color = VitalColors.black,
        fontSize = 84.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        letterSpacing = -0.045.em
    ))

    val innerUnitLayout = textMeasurer.measure(innerUnit, TextStyle(
        color = VitalColors.black.copy(alpha = 0.7f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        letterSpacing = 0.5.sp
    ))

    val topPillLayout = textMeasurer.measure(topPill, TextStyle(
        color = VitalColors.black,
        fontSize = 21.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    ))

    val pillStyle = TextStyle(
        color = VitalColors.black,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        letterSpacing = 0.24.em
    )
    val pillLayout = textMeasurer.measure(pillText, pillStyle)

    DesignUnitsProvider {
        val scale = designUnitsScale()
        val density = androidx.compose.ui.platform.LocalDensity.current.density

        // Dial center and radii
        val dialCenterX = DialTicks.CENTER_X.du(density)
        val dialCenterY = DialTicks.CENTER_Y.du(density)
        val dialRadius = DialTicks.DIAL_RADIUS.du(density)
        val innerRadius = DialTicks.INNER_RADIUS.du(density)
        val innerRingRadius = DialTicks.INNER_RING_RADIUS.du(density)
        val dialCenter = Offset(dialCenterX, dialCenterY)

        // Cache gradients
        val dialFaceBrush = remember(dialRadius, dialCenter) {
            Brush.radialGradient(
                0f to VitalColors.DialFaceStop0,
                0.86f to VitalColors.DialFaceStop1,
                0.93f to VitalColors.DialFaceStop2,
                0.975f to VitalColors.DialFaceStop3,
                1f to VitalColors.DialFaceStop4,
                center = dialCenter,
                radius = dialRadius
            )
        }

        val innerFaceBrush = remember(innerRadius, dialCenter) {
            Brush.radialGradient(
                0f to VitalColors.InnerFaceStop0,
                0.72f to VitalColors.InnerFaceStop1,
                0.93f to VitalColors.InnerFaceStop2,
                1f to VitalColors.InnerFaceStop3,
                center = dialCenter,
                radius = innerRadius
            )
        }

        // Cache tick paths
        val rimTickData = remember { buildTickData(DialTicks.RIM, dialCenter, density, scale) }
        val barsTickData = remember { buildTickData(DialTicks.BARS, dialCenter, density, scale) }
        val midTickData = remember { buildTickData(DialTicks.MID, dialCenter, density, scale) }
        val fineTickData = remember { buildTickData(DialTicks.FINE, dialCenter, density, scale) }

        Canvas(
            modifier = modifier.fillMaxSize()
        ) {
            // ===== DIAL FACE (outer disc with radial gradient) =====
            drawCircle(
                brush = dialFaceBrush,
                center = dialCenter,
                radius = dialRadius
            )

            // ===== INNER DIAL =====
            // Black rim band
            drawCircle(
                color = VitalColors.InnerDialRim,
                center = dialCenter,
                radius = innerRadius
            )
            // Inner face gradient
            drawCircle(
                brush = innerFaceBrush,
                center = dialCenter,
                radius = innerRingRadius + 0.5f * scale * density
            )
            // Hairline ring
            drawCircle(
                color = VitalColors.RefRingLine,
                center = dialCenter,
                radius = innerRingRadius,
                style = Stroke(0.8f * scale * density)
            )

            // ===== TICK TRACKS (RIM, BARS, MID, FINE) =====
            drawTickPaths(rimTickData, VitalColors.RefInk, density, scale)
            drawTickPathsWithColors(barsTickData, density, scale)
            drawTickPathsWithColors(midTickData, density, scale)
            drawTickPathsWithColors(fineTickData, density, scale)

            // ===== KNURLED KNOB (decorative, no interaction) =====
            // Fixed at Walk activity (needle at 0°)
            drawKnurledKnobDecorative(density, scale)

            // ===== FAB (decorative, no interaction) =====
            drawFabDecorative(density, scale, letters, highlightedIndex, fabGlyph, labelLayouts)

            // ===== TEXT ELEMENTS =====
            drawTextElements(
                innerLabelLayout,
                innerValueLayout,
                innerUnitLayout,
                topPillLayout,
                pillLayout,
                dialCenterX,
                dialCenterY,
                density,
                scale
            )
        }
    }
}

/**
 * FAB glyph options
 */
enum class FabGlyph { Plus, Camera }

/**
 * Build tick data with paths for a track - cached for performance
 */
private fun buildTickData(
    ticks: List<DialTick>,
    center: Offset,
    density: Float,
    scale: Float
): List<Pair<Path, DialTick>> {
    return ticks.map { tick ->
        val angle = tick.angleDeg * PI.toFloat() / 180f
        val r0 = tick.r0 * scale * density
        val r1 = tick.r1 * scale * density
        val x0 = center.x + r0 * cos(angle)
        val y0 = center.y + r0 * sin(angle)
        val x1 = center.x + r1 * cos(angle)
        val y1 = center.y + r1 * sin(angle)
        val path = Path().apply {
            moveTo(x0, y0)
            lineTo(x1, y1)
        }
        path to tick
    }
}

/**
 * Draw tick paths with a single color
 */
private fun DrawScope.drawTickPaths(
    paths: List<Pair<Path, DialTick>>,
    color: Color,
    density: Float,
    scale: Float
) {
    val minStrokeWidth = 0.7f * scale * density
    paths.forEach { (path, tick) ->
        val strokeWidth = max(minStrokeWidth, tick.width * DialTicks.TICK_WIDTH_SCALE * scale * density)
        drawPath(
            path,
            color = color,
            style = Stroke(strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

/**
 * Draw tick paths with their individual colors and widths
 */
private fun DrawScope.drawTickPathsWithColors(
    paths: List<Pair<Path, DialTick>>,
    density: Float,
    scale: Float
) {
    val minStrokeWidth = 0.7f * scale * density
    paths.forEach { (path, tick) ->
        val strokeWidth = max(minStrokeWidth, tick.width * DialTicks.TICK_WIDTH_SCALE * scale * density)
        val color = Color(tick.argb)
        drawPath(
            path,
            color = color,
            style = Stroke(strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

/**
 * Draw the knurled knob decorative (fixed at walk activity, no needle rotation, no interaction)
 */
private fun DrawScope.drawKnurledKnobDecorative(density: Float, scale: Float) {
    fun px(v: Float): Float = v * scale * density

    val knobX = px(KNOB_X)
    val knobY = px(KNOB_Y)
    val knobCenter = Offset(knobX, knobY)
    val knobOuter = px(KNOB_OUTER)
    val knobTips = px(KNOB_TIPS)
    val knobToothDepth = px(KNOB_TOOTH_DEPTH)
    val knobPin = px(KNOB_PIN)

    fun polar(rDu: Float, angleDeg: Float): Offset {
        val a = angleDeg * PI.toFloat() / 180f
        return Offset(knobX + px(rDu) * cos(a), knobY + px(rDu) * sin(a))
    }

    // Dark ring (outer)
    drawCircle(color = VitalColors.KnobInk, center = knobCenter, radius = knobOuter)

    // Serrated orange disc
    val scallopPath = Path().apply {
        val steps = KNOB_TEETH * 6
        for (i in 0 until steps) {
            val a = (i / steps.toFloat()) * (PI * 2).toFloat()
            val rr = knobTips - knobToothDepth * (0.5f + 0.5f * cos(a * KNOB_TEETH))
            val x = knobX + rr * cos(a)
            val y = knobY + rr * sin(a)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(scallopPath, color = VitalColors.KnobOrange)

    // Centre pin
    drawCircle(color = VitalColors.KnobInk, center = knobCenter, radius = knobPin)

    // Needle fixed at walk (0° rotation)
    withTransform({ rotate(0f, knobCenter) }) {
        drawLine(
            color = VitalColors.KnobInk,
            start = Offset(knobX + px(29.1f), knobY + px(28.4f)),
            end = Offset(knobX + px(41.0f), knobY + px(40.4f)),
            strokeWidth = px(3.4f),
            cap = StrokeCap.Round
        )
    }

    // Activity selector: arc + radial spokes
    val arcR = px(ARC_R_DU)
    drawArc(
        color = VitalColors.KnobOrange,
        startAngle = ActivityGlyphs.WALK_ANGLE_DEG,
        sweepAngle = ActivityGlyphs.all.last().angleDeg - ActivityGlyphs.all.first().angleDeg,
        useCenter = false,
        topLeft = Offset(knobX - arcR, knobY - arcR),
        size = Size(arcR * 2f, arcR * 2f),
        style = Stroke(px(2.1f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    ActivityGlyphs.all.forEach { a ->
        drawLine(
            color = VitalColors.KnobOrange,
            start = polar(ARC_R_DU, a.angleDeg),
            end = polar(ARC_R_DU + SPOKE_LEN_DU, a.angleDeg),
            strokeWidth = px(2.1f),
            cap = StrokeCap.Round
        )
    }

    // Activity icons
    ActivityGlyphs.all.forEachIndexed { i, activity ->
        val centre = activity.centreDu()
        val pxCentreX = px(centre.x)
        val pxCentreY = px(centre.y)
        val s = px(ICON_SCALE)
        withTransform({ translate(pxCentreX, pxCentreY); scale(s, s, pivot = Offset.Zero); translate(-12f, -12f) }) {
            drawPath(activity.path, color = VitalColors.ActivityFanIcon)
        }
    }
}

/**
 * Draw FAB decorative with L/M/H labels and selected glyph
 */
private fun DrawScope.drawFabDecorative(
    density: Float,
    scale: Float,
    letters: List<String>,
    highlightedIndex: Int,
    fabGlyph: FabGlyph,
    labelLayouts: List<TextLayoutResult>
) {
    fun px(v: Float): Float = v * scale * density

    val x0 = px(FAB_X0)
    val y0 = px(FAB_Y0)
    val s = px(FAB_S)
    val r = s / 2f

    fun teardropPath(dx: Float, dy: Float): Path {
        val rect = Rect(x0 + dx, y0 + dy, x0 + s + dx, y0 + s + dy)
        return Path().apply {
            moveTo(x0 + r + dx, y0 + dy)
            lineTo(x0 + s + dx, y0 + dy)
            lineTo(x0 + s + dx, y0 + r + dy)
            arcTo(rect, 0f, 90f, false)
            arcTo(rect, 90f, 90f, false)
            arcTo(rect, 180f, 90f, false)
            close()
        }
    }

    // Soft shadow
    val shadowPath = teardropPath(px(3f), px(10f))
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb((0.55f * 255f * 0.75f).toInt(), 120, 20, 0)
            maskFilter = BlurMaskFilter(px(7f), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawPath(shadowPath.asAndroidPath(), paint)
    }

    // Body
    drawPath(teardropPath(0f, 0f), color = VitalColors.RefInk)

    // Corner bracket (orange 2.3, butt)
    val bracket = Path().apply {
        moveTo(px(70.06f), px(750.46f))
        lineTo(px(83.32f), px(750.46f))
        moveTo(px(82.17f), px(749.31f))
        lineTo(px(82.17f), px(762.56f))
    }
    drawPath(bracket, color = VitalColors.RefOrange, style = Stroke(px(2.3f), cap = StrokeCap.Butt))

    // Glyph on FAB - Plus or Camera (using Walk glyph as base, tinted orange)
    val gs = px(40f / 24f * 1.13f)
    withTransform({ translate(px(52.20f), px(781.86f)); scale(gs, gs, pivot = Offset.Zero); translate(-12f, -12f) }) {
        when (fabGlyph) {
            FabGlyph.Plus -> drawPath(ActivityGlyphs.Walk.path, color = VitalColors.FabIcon)
            FabGlyph.Camera -> drawPath(ActivityGlyphs.Run.path, color = VitalColors.FabIcon)
        }
    }

    // L / M / H leader lines
    val leaders = Path().apply {
        moveTo(px(50.22f), px(727.97f))
        lineTo(px(50.22f), px(736.30f))
        cubicTo(px(50.22f), px(737.64f), px(52.18f), px(738.43f), px(54.58f), px(738.51f))
        cubicTo(px(64.11f), px(739.30f), px(71.65f), px(741.88f), px(77.60f), px(745.65f))
        moveTo(px(88.12f), px(745.09f))
        lineTo(px(91.69f), px(741.51f))
        moveTo(px(87.33f), px(748.63f))
        cubicTo(px(91.49f), px(754.59f), px(93.48f), px(768.08f), px(95.86f), px(781.73f))
        lineTo(px(104.98f), px(781.91f))
    }
    drawPath(leaders, color = VitalColors.RefInk, style = Stroke(px(2.1f), cap = StrokeCap.Butt))

    // L / M / H labels with highlight - use pre-measured layouts
    letters.forEachIndexed { index, _ ->
        val layoutResult = labelLayouts[index]
        val pos = LABEL_POSITIONS[index]
        val left = px(pos.x) - layoutResult.size.width / 2f
        drawText(layoutResult, topLeft = Offset(left, px(pos.y) - layoutResult.firstBaseline))
    }
}

/**
 * Draw text elements for OrangeDial - uses pre-measured layouts
 */
private fun DrawScope.drawTextElements(
    innerLabelLayout: TextLayoutResult,
    innerValueLayout: TextLayoutResult,
    innerUnitLayout: TextLayoutResult,
    topPillLayout: TextLayoutResult,
    pillLayout: TextLayoutResult,
    dialCenterX: Float,
    dialCenterY: Float,
    density: Float,
    scale: Float
) {
    fun px(v: Float): Float = v * scale * density

    fun drawLayout(layout: TextLayoutResult, x: Float, baselineY: Float, isCentered: Boolean = false) {
        val left = px(x) - if (isCentered) layout.size.width / 2f else 0f
        drawText(layout, topLeft = Offset(left, px(baselineY) - layout.firstBaseline))
    }

    // Inner label - centered above center
    drawLayout(innerLabelLayout, dialCenterX, dialCenterY - 35f, isCentered = true)

    // Inner value - large, centered
    drawLayout(innerValueLayout, dialCenterX, dialCenterY + 5f, isCentered = true)

    // Inner unit - small, below value
    drawLayout(innerUnitLayout, dialCenterX, dialCenterY + 55f, isCentered = true)

    // Top pill (e.g., "MOVE")
    drawLayout(topPillLayout, 29.3f, 143.3f)

    // Bottom pill (e.g., "TODAY") - centered, rounded rect with stroke
    val dialCenterPxX = dialCenterX
    val pillWidth = pillLayout.size.width + px(24f)
    val pillHeight = px(26f)
    val pillX = dialCenterPxX - pillWidth / 2f
    val pillY = px(710f)

    // Draw pill background (transparent fill)
    drawRoundRect(
        color = Color.Transparent,
        topLeft = Offset(pillX, pillY),
        size = Size(pillWidth, pillHeight),
        cornerRadius = CornerRadius(pillHeight / 2)
    )

    // Draw pill stroke (black border)
    drawRoundRect(
        color = VitalColors.black,
        topLeft = Offset(pillX, pillY),
        size = Size(pillWidth, pillHeight),
        cornerRadius = CornerRadius(pillHeight / 2),
        style = Stroke(px(3f))
    )

    // Pill text centered
    val textLeft = dialCenterPxX - pillLayout.size.width / 2f
    val textBaseline = pillY + pillHeight / 2f + pillLayout.firstBaseline - pillLayout.size.height / 2f
    drawText(pillLayout, topLeft = Offset(textLeft, textBaseline))
}

@Preview
@Composable
fun OrangeDialPreview_Meals() {
    com.vital.ui.theme.VitalTheme(true) {
        OrangeDial(
            modifier = Modifier.fillMaxSize(),
            letters = listOf("L", "M", "H"),
            highlightedIndex = 1,
            innerLabel = "KCAL LEFT",
            innerValue = "1,240",
            innerUnit = "KCAL",
            pillText = "TODAY",
            topPill = "MOVE",
            fabGlyph = FabGlyph.Plus
        )
    }
}

@Preview
@Composable
fun OrangeDialPreview_Lift() {
    com.vital.ui.theme.VitalTheme(true) {
        OrangeDial(
            modifier = Modifier.fillMaxSize(),
            letters = listOf("L", "M", "H"),
            highlightedIndex = 1,
            innerLabel = "SETS LEFT",
            innerValue = "12",
            innerUnit = "SETS",
            pillText = "TODAY",
            topPill = "LIFT",
            fabGlyph = FabGlyph.Camera
        )
    }
}

@Preview
@Composable
fun OrangeDialPreview_Cardio() {
    com.vital.ui.theme.VitalTheme(true) {
        OrangeDial(
            modifier = Modifier.fillMaxSize(),
            letters = listOf("L", "M", "H"),
            highlightedIndex = 0,
            innerLabel = "KM LEFT",
            innerValue = "8.5",
            innerUnit = "KM",
            pillText = "THIS WEEK",
            topPill = "CARDIO",
            fabGlyph = FabGlyph.Plus
        )
    }
}