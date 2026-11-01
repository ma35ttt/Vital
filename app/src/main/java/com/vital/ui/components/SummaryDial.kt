package com.vital.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.vital.ui.theme.du
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import com.vital.ui.theme.designUnitsScale
import com.vital.ui.components.ActivityGlyphs
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// Period enum moved to top-level for reuse across screens
enum class Period { L, M, H }

// Knob + FAB measured values from vital-ui/src/knob.js and fab.js (du)
private const val KNOB_X = 87.85f
private const val KNOB_Y = 295.1f
private const val KNOB_OUTER = 74.61f
private const val KNOB_TIPS = 70.3f
private const val KNOB_TEETH = 60
private const val KNOB_TOOTH_DEPTH = 1.23f
private const val KNOB_PIN = 11.31f
// 5 activities: steps, walk, run, bike, moto (from knob.js)
private val ACTIVITY_ANGLES = listOf(16.3f, 31.25f, 46.2f, 61.15f, 76.1f)
private val ACTIVITY_ICON_ANGLES = listOf(17.4f, 30.7f, 45.5f, 59.2f, 72.0f)
private const val ARC_R_DU = 92.9f
private const val SPOKE_LEN_DU = 17.9f
private const val ICON_R_DU = 123.4f
private const val ICON_SCALE = 0.66f

// Needle rotation: (activityAngle - walkAngle) * 0.9
// steps: (16.3 - 31.25) * 0.9 = -13.425°
// walk: 0°
// run: (46.2 - 31.25) * 0.9 = 13.425°
// bike: (61.15 - 31.25) * 0.9 = 26.91°
// moto: (76.1 - 31.25) * 0.9 = 40.365°
private const val NEEDLE_MIN_DEG = -13.425f
private const val NEEDLE_MAX_DEG = 40.365f
private const val WALK_ANGLE_DEG = 31.25f

@Composable
fun SummaryDial(
    modifier: Modifier = Modifier,
    kcalRemaining: Float,
    kcalTarget: Float,
    proteinPercent: Float, // 0-1
    carbsPercent: Float,
    fatPercent: Float,
    period: Period = Period.M,
    onPeriodChange: (Period) -> Unit,
    accentColor: androidx.compose.ui.graphics.Color,
    showKnob: Boolean = true,
    onKnobProgressChange: ((Float) -> Unit)? = null
) {
    val animatedKcal by animateFloatAsState(
        targetValue = kcalRemaining,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 100f)
    )
    val textMeasurer = rememberTextMeasurer()

    // Use DesignUnits for all measurements
    DesignUnitsProvider {
        val scale = designUnitsScale()
        val density = LocalDensity.current.density

        var activityIndex by remember { mutableStateOf(1) } // walk
        var dragNeedleAngle by remember { mutableStateOf(0f) }
        var isDragging by remember { mutableStateOf(false) }
        val knobCenterPx = Offset(KNOB_X.du(density), KNOB_Y.du(density))
        val knobHitRadiusPx = (KNOB_OUTER + ICON_R_DU + 25f).du(density)

        // Pre-calculate dial center and radii in pixels
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

        // Cache tick paths with their data for each track
        val rimTickData = remember { buildTickData(DialTicks.RIM, dialCenter, density, scale) }
        val barsTickData = remember { buildTickData(DialTicks.BARS, dialCenter, density, scale) }
        val midTickData = remember { buildTickData(DialTicks.MID, dialCenter, density, scale) }
        val fineTickData = remember { buildTickData(DialTicks.FINE, dialCenter, density, scale) }

        Canvas(
            modifier = modifier.fillMaxSize().pointerInput(Unit) {

                fun angleToActivityIndex(angleDeg: Float): Int {
                    var best = 0
                    var bestDiff = 360f
                    ACTIVITY_ANGLES.forEachIndexed { i, a ->
                        var d = abs(angleDeg - a)
                        if (d > 180f) d = 360f - d
                        if (d < bestDiff) { bestDiff = d; best = i }
                    }
                    return best
                }

                fun selectActivityAt(pos: Offset) {
                    val dx = pos.x - knobCenterPx.x
                    val dy = pos.y - knobCenterPx.y
                    if (sqrt(dx * dx + dy * dy) > knobHitRadiusPx) return
                    var deg = atan2(dy, dx) * 180f / PI.toFloat()
                    if (deg < 0f) deg += 360f
                    // Convert to needle angle: (activityAngle - walkAngle) * 0.9
                    val needleDeg = (deg - WALK_ANGLE_DEG) * 0.9f
                    val clamped = needleDeg.coerceIn(NEEDLE_MIN_DEG, NEEDLE_MAX_DEG)
                    dragNeedleAngle = clamped
                    if (!isDragging) {
                        // Tap: snap immediately
                        val idx = angleToActivityIndex(deg)
                        if (idx != activityIndex) {
                            activityIndex = idx
                            onKnobProgressChange?.invoke(idx.toFloat() / (ACTIVITY_ANGLES.size - 1))
                        }
                    }
                }

                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        selectActivityAt(it)
                    },
                    onDrag = { change, _ ->
                        selectActivityAt(change.position)
                    },
                    onDragEnd = {
                        isDragging = false
                        // Snap to nearest activity
                        val snappedIdx = angleToActivityIndex(WALK_ANGLE_DEG + dragNeedleAngle / 0.9f)
                        if (snappedIdx != activityIndex) {
                            activityIndex = snappedIdx
                            onKnobProgressChange?.invoke(snappedIdx.toFloat() / (ACTIVITY_ANGLES.size - 1))
                        }
                    }
                )
            }
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
                radius = innerRingRadius + 0.5f * scale * density // +0.5 du as in reference
            )
            // Hairline ring
            drawCircle(
                color = VitalColors.RefRingLine,
                center = dialCenter,
                radius = innerRingRadius,
                style = Stroke(0.8f * scale * density)
            )

            // ===== TICK TRACKS (RIM, BARS, MID, FINE) =====
            // RIM track - dark gray ticks (use single color)
            drawTickPaths(rimTickData, VitalColors.RefInk, density, scale)
            // BARS track - orange/red ticks with individual colors
            drawTickPathsWithColors(barsTickData, density, scale)
            // MID track - dark red/brown ticks with individual colors
            drawTickPathsWithColors(midTickData, density, scale)
            // FINE track - fine dark ticks with individual colors
            drawTickPathsWithColors(fineTickData, density, scale)

            // ===== KNURLED KNOB =====
            if (showKnob) {
                val currentNeedleAngle = if (isDragging) dragNeedleAngle else (ACTIVITY_ANGLES[activityIndex] - WALK_ANGLE_DEG) * 0.9f
                drawKnurledKnob(density, scale, currentNeedleAngle)
            }

            // ===== FAB (bottom-left teardrop, from fab.js) =====
            drawFab(density, scale)

            // ===== TEXTS AND UI ELEMENTS (reference coordinates) =====
            drawTextElements(textMeasurer, animatedKcal, density, scale)
        }
    }
}

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
 * Draw the knurled knob + activity selector from knob.js (all values du).
 * Needle rotates to the selected activity: (angle - walk) * 0.9 degrees.
 */
private fun DrawScope.drawKnurledKnob(density: Float, scale: Float, needleAngleDeg: Float) {
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

    // Serrated orange disc (scallopPath() from knob.js)
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

    // Needle (rotates with the selected activity) - line from knob center offset (+29.1, +28.4) to (+41.0, +40.4) du at rotation 0
    withTransform({ rotate(needleAngleDeg, knobCenter) }) {
        drawLine(
            color = VitalColors.KnobInk,
            start = Offset(knobX + px(29.1f), knobY + px(28.4f)),
            end = Offset(knobX + px(41.0f), knobY + px(40.4f)),
            strokeWidth = px(3.4f),
            cap = StrokeCap.Round
        )
    }

    // Activity selector: arc + radial spokes (orange 2.1, round caps/joins)
    val arcR = px(ARC_R_DU)
    drawArc(
        color = VitalColors.KnobOrange,
        startAngle = ACTIVITY_ANGLES.first(),
        sweepAngle = ACTIVITY_ANGLES.last() - ACTIVITY_ANGLES.first(),
        useCenter = false,
        topLeft = Offset(knobX - arcR, knobY - arcR),
        size = Size(arcR * 2f, arcR * 2f),
        style = Stroke(px(2.1f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    ACTIVITY_ANGLES.forEach { a ->
        drawLine(
            color = VitalColors.KnobOrange,
            start = polar(ARC_R_DU, a),
            end = polar(ARC_R_DU + SPOKE_LEN_DU, a),
            strokeWidth = px(2.1f),
            cap = StrokeCap.Round
        )
    }

    // Activity icons at ICON_R along each iconAngle - use ActivityGlyphs
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
 * Bottom-left FAB from fab.js: teardrop (square top-right corner) with soft shadow,
 * orange corner bracket, walk glyph and the L/M/H leader lines.
 * The L/M/H text labels are drawn in drawTextElements (needs the text measurer).
 */
private fun DrawScope.drawFab(density: Float, scale: Float) {
    fun px(v: Float): Float = v * scale * density
    val x0 = px(13.50f)
    val y0 = px(745.29f)
    val s = px(74.43f)
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

    // Soft shadow: blurred dark-orange copy offset (+3, +10), feGaussianBlur stdDev 7, opacity .75
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

    // Walk glyph (orange), scale 40/24*1.13, centred at (52.20, 781.86)
    val gs = px(40f / 24f * 1.13f)
    withTransform({ translate(px(52.20f), px(781.86f)); scale(gs, gs, pivot = Offset.Zero); translate(-12f, -12f) }) {
        drawPath(ActivityGlyphs.Walk.path, color = VitalColors.FabIcon)
    }

    // L / M / H leader lines (ink 2.1, butt) - Q converted to cubic
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
}

/**
 * Draw text elements on the dial using reference coordinates (du)
 * Measure text first with textMeasurer, then draw with layout result
 */
private fun DrawScope.drawTextElements(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    animatedKcal: Float,
    density: Float,
    scale: Float
) {
    // du -> px: design frame is 393 du wide, scale = screenWidthDp / 393
    fun px(v: Float): Float = v * scale * density

    // Reference text y values are SVG baselines; drawText takes the layout
    // top-left, so topLeft = Offset(left, baselineY - firstBaseline), and
    // centred text subtracts layout width / 2 from x.
    fun drawMeasuredText(text: String, style: TextStyle, x: Float, baselineY: Float, isCentered: Boolean = false) {
        val layoutResult = textMeasurer.measure(text, style)
        val left = px(x) - if (isCentered) layoutResult.size.width / 2f else 0f
        drawText(layoutResult, topLeft = Offset(left, px(baselineY) - layoutResult.firstBaseline))
    }

    val calText = "${animatedKcal.toInt()}cal"

    // ===== CHIPS / PILLS (drawn first, under the text - shapes sit under text in the reference) =====
    val strokeWidth1 = px(2.6f)
    val strokeWidth2 = px(1.7f)

    // Day chip (orange-chip) - SUN rect (13.69, 70.86, 58.95×24.41), r 4
    drawRoundRect(
        color = VitalColors.RefOrangeChip,
        topLeft = Offset(px(13.69f), px(70.86f)),
        size = Size(px(58.95f), px(24.41f)),
        cornerRadius = CornerRadius(px(4f))
    )

    // Date chip (orange-chip) - APR.9 (320.35, 70.86), r 4
    drawRoundRect(
        color = VitalColors.RefOrangeChip,
        topLeft = Offset(px(320.35f), px(70.86f)),
        size = Size(px(58.95f), px(24.41f)),
        cornerRadius = CornerRadius(px(4f))
    )

    // SHOW MORE box (260.2, 155.4, 119.69×35.13), r 7.5, stroke 2.6
    drawRoundRect(
        color = VitalColors.RefInk,
        topLeft = Offset(px(260.2f), px(155.4f)),
        size = Size(px(119.69f), px(35.13f)),
        cornerRadius = CornerRadius(px(7.5f)),
        style = Stroke(strokeWidth1)
    )

    // Time pill (241.74, 286.4, 97.06×27.39), r 8.5
    val timePillBrush = Brush.verticalGradient(
        0f to Color(0xFF3A4144),
        0.1f to Color(0xFF5B656C),
        0.25f to Color(0xFF5F6A72),
        0.75f to Color(0xFF5E6870),
        0.9f to Color(0xFF5A646B),
        1f to Color(0xFF3C4346),
    )
    drawRoundRect(
        brush = timePillBrush,
        topLeft = Offset(px(241.74f), px(286.4f)),
        size = Size(px(97.06f), px(27.39f)),
        cornerRadius = CornerRadius(px(8.5f))
    )

    // BPM pill (242.3, 562.7, 97.06×28.58), r 14.3
    drawRoundRect(
        color = VitalColors.RefOrange,
        topLeft = Offset(px(242.3f), px(562.7f)),
        size = Size(px(97.06f), px(28.58f)),
        cornerRadius = CornerRadius(px(14.3f))
    )

    // Pointer notch on dial's left edge
    // M13.69,475.76 L28.88,498.38 L13.69,521 Z
    drawPath(
        Path().apply {
            moveTo(px(13.69f), px(475.76f))
            lineTo(px(28.88f), px(498.38f))
            lineTo(px(13.69f), px(521f))
            close()
        },
        color = VitalColors.RefOrange
    )
    // Chevron M13.4,487.67 L22.92,498.38 L13.4,509.4 (stroke 1.7)
    drawPath(
        Path().apply {
            moveTo(px(13.4f), px(487.67f))
            lineTo(px(22.92f), px(498.38f))
            lineTo(px(13.4f), px(509.4f))
        },
        color = VitalColors.RefInk,
        style = Stroke(strokeWidth2, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
    )

    // SUMMARY title - centre 197.08, y 91.4 (mono 21.5/500, ls 2.85)
    drawMeasuredText(
        text = "SUMMARY",
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 21.5f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.85f.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 197.08f,
        baselineY = 91.4f,
        isCentered = true
    )

    // SUN - centre 43.17, y 88.72 (mono 16/700, ls 0.5)
    drawMeasuredText(
        text = "SUN",
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 16f.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5f.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 43.17f,
        baselineY = 88.72f,
        isCentered = true
    )

    // APR.9 - centre 349.82, y 88.72 (mono 16/700, ls 0.5)
    drawMeasuredText(
        text = "APR.9",
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 16f.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5f.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 349.82f,
        baselineY = 88.72f,
        isCentered = true
    )

    // MOVE - 13.69, y 138.14 (22/500)
    drawMeasuredText(
        text = "MOVE",
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 22f.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 13.69f,
        baselineY = 138.14f
    )

    // Calories value - 15.48, y 191.73 (60.5/600, ls -1.2)
    drawMeasuredText(
        text = calText,
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 60.5f.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = -1.2f.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 15.48f,
        baselineY = 191.73f
    )

    // SHOW MORE - centre 320.06, y 177.44 (mono 14/500, ls 2.2)
    drawMeasuredText(
        text = "SHOW MORE",
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 14f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.2f.sp,
            fontFamily = VitalFonts.JetBrainsMonoMedium
        ),
        x = 320.06f,
        baselineY = 177.44f,
        isCentered = true
    )

    // 09:24 - centre 290.28, y 307.55 (mono 22/500)
    drawMeasuredText(
        text = "09:24",
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 22f.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = VitalFonts.JetBrainsMonoMedium
        ),
        x = 290.28f,
        baselineY = 307.55f,
        isCentered = true
    )

    // 4.98 km - 43.47, y 509.1 (28.5/500)
    drawMeasuredText(
        text = "4.98 km",
        style = TextStyle(
            color = VitalColors.RefWhite,
            fontSize = 28.5f.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 43.47f,
        baselineY = 509.1f
    )

    // STEPS - 203.05, y 466.2 (16/500, ls -1)
    drawMeasuredText(
        text = "STEPS",
        style = TextStyle(
            color = VitalColors.RefLabelGray,
            fontSize = 16f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = -1f.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 203.05f,
        baselineY = 466.2f
    )

    // Steps value 11.624 - 200.67, y 516.2 (mono 49/500, ls 1.8)
    drawMeasuredText(
        text = "11.624",
        style = TextStyle(
            color = VitalColors.RefWhite,
            fontSize = 49f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.8f.sp,
            fontFamily = VitalFonts.JetBrainsMonoMedium
        ),
        x = 200.67f,
        baselineY = 516.2f
    )

    // 128bpm - centre 290.87, y 582.9 (17/400)
    drawMeasuredText(
        text = "128bpm",
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 17f.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        x = 290.87f,
        baselineY = 582.9f,
        isCentered = true
    )

    // ===== FAB L / M / H labels (fab.js: sans 13.2/500 ink, anchor middle; project mono kept) =====
    val fabLabelStyle = TextStyle(
        color = VitalColors.RefInk,
        fontSize = 13.2f.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
    drawMeasuredText("L", fabLabelStyle, x = 50.01f, baselineY = 723.33f, isCentered = true)
    drawMeasuredText("M", fabLabelStyle, x = 103.01f, baselineY = 737.65f, isCentered = true)
    drawMeasuredText("H", fabLabelStyle, x = 117.09f, baselineY = 789.86f, isCentered = true)
}

@Composable
@Preview
fun SummaryDialPreview() {
    var period by remember { mutableStateOf(Period.M) }
    var knobProgress by remember { mutableStateOf(0.5f) }

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = VitalColors.Background
    ) {
        SummaryDial(
            modifier = Modifier.fillMaxSize(),
            kcalRemaining = 306f,
            kcalTarget = 2500f,
            proteinPercent = 0.3f,
            carbsPercent = 0.4f,
            fatPercent = 0.3f,
            period = period,
            onPeriodChange = { period = it },
            accentColor = VitalColors.AccentTDY,
            onKnobProgressChange = { knobProgress = it }
        )
    }
}