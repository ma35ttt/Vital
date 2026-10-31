package com.vital.ui.components

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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.vital.ui.theme.du
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.designUnitsScale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// Period enum moved to top-level for reuse across screens
enum class Period { L, M, H }

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
    var currentPeriod by remember { mutableStateOf(period) }
    var knobProgress by remember { mutableStateOf(0.5f) }

    val animatedKcal by animateFloatAsState(
        targetValue = kcalRemaining,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 100f)
    )
    val textMeasurer = rememberTextMeasurer()

    // Use DesignUnits for all measurements
    DesignUnitsProvider {
        val scale = designUnitsScale()

        // Pre-calculate dial center and radii in pixels
        val dialCenterX = DialTicks.CENTER_X.du()
        val dialCenterY = DialTicks.CENTER_Y.du()
        val dialRadius = DialTicks.DIAL_RADIUS.du()
        val innerRadius = DialTicks.INNER_RADIUS.du()
        val innerRingRadius = DialTicks.INNER_RING_RADIUS.du()
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
        val rimTickData = remember { buildTickData(DialTicks.RIM, dialCenter, scale) }
        val barsTickData = remember { buildTickData(DialTicks.BARS, dialCenter, scale) }
        val midTickData = remember { buildTickData(DialTicks.MID, dialCenter, scale) }
        val fineTickData = remember { buildTickData(DialTicks.FINE, dialCenter, scale) }

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            // Handle period switch drag (bottom left area)
                            val width = size.width
                            val height = size.height
                            val switchY = height * 0.85f
                            val switchHeight = height * 0.12f
                            if (change.position.y > switchY && change.position.y < switchY + switchHeight) {
                                val third = width / 3
                                val newPeriod = when {
                                    change.position.x < third -> Period.L
                                    change.position.x < 2 * third -> Period.M
                                    else -> Period.H
                                }
                                if (newPeriod != currentPeriod) {
                                    currentPeriod = newPeriod
                                    onPeriodChange(newPeriod)
                                }
                            }
                        }
                    )
                }
        ) {
            val width = size.width.toFloat()
            val height = size.height.toFloat()

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
                radius = innerRingRadius + 0.5f * scale // +0.5 du as in reference
            )
            // Hairline ring
            drawCircle(
                color = VitalColors.RefRingLine,
                center = dialCenter,
                radius = innerRingRadius,
                style = Stroke(0.8f * scale)
            )

            // ===== TICK TRACKS (RIM, BARS, MID, FINE) =====
            // RIM track - dark gray ticks (use single color)
            drawTickPaths(rimTickData, VitalColors.RefInk, scale)
            // BARS track - orange/red ticks with individual colors
            drawTickPathsWithColors(barsTickData, scale)
            // MID track - dark red/brown ticks with individual colors
            drawTickPathsWithColors(midTickData, scale)
            // FINE track - fine dark ticks with individual colors
            drawTickPathsWithColors(fineTickData, scale)

            // ===== KNURLED KNOB =====
            if (showKnob) {
                drawKnurledKnob(scale, knobProgress)
            }

            // ===== TEXTS AND UI ELEMENTS =====
            drawTextElements(textMeasurer, animatedKcal, kcalTarget, currentPeriod, onPeriodChange, accentColor, scale, width, height)
        }
    }
}

/**
 * Build tick data with paths for a track - cached for performance
 */
private fun buildTickData(
    ticks: List<DialTick>,
    center: Offset,
    scale: Float
): List<Pair<Path, DialTick>> {
    return ticks.map { tick ->
        val angle = (tick.angleDeg - 90f) * PI.toFloat() / 180f
        val r0 = tick.r0 * scale
        val r1 = tick.r1 * scale
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
    scale: Float
) {
    val minStrokeWidth = 0.7f * scale
    paths.forEach { (path, tick) ->
        val strokeWidth = max(minStrokeWidth, tick.width * DialTicks.TICK_WIDTH_SCALE * scale)
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
    scale: Float
) {
    val minStrokeWidth = 0.7f * scale
    paths.forEach { (path, tick) ->
        val strokeWidth = max(minStrokeWidth, tick.width * DialTicks.TICK_WIDTH_SCALE * scale)
        val color = Color(tick.argb)
        drawPath(
            path,
            color = color,
            style = Stroke(strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

/**
 * Draw the knurled knob at the reference position
 */
private fun DrawScope.drawKnurledKnob(scale: Float, knobProgress: Float) {
    val knobX = 87.85f * scale
    val knobY = 295.1f * scale
    val knobCenter = Offset(knobX, knobY)
    val knobOuter = 74.61f * scale
    val knobTips = 70.3f * scale
    val knobTeeth = 60
    val knobToothDepth = 1.23f * scale
    val knobPin = 11.31f * scale

    // Dark ring (outer)
    drawCircle(
        color = VitalColors.KnobInk,
        center = knobCenter,
        radius = knobOuter
    )

    // Serrated orange disc (scalloped edge)
    val scallopPath = Path().apply {
        val steps = knobTeeth * 6
        for (i in 0 until steps) {
            val a = (i / steps.toFloat()) * (PI * 2).toFloat()
            val rr = knobTips - knobToothDepth * (0.5f + 0.5f * cos(a * knobTeeth))
            val x = knobX + rr * cos(a)
            val y = knobY + rr * sin(a)
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    drawPath(scallopPath, color = VitalColors.KnobOrange)

    // Centre pin
    drawCircle(
        color = VitalColors.KnobInk,
        center = knobCenter,
        radius = knobPin
    )

    // Needle (rotates with knobProgress)
    // Map knobProgress (0-1) to activity angle range
    // Activities: steps=16.3, walk=31.25, run=46.2, bike=61.15, moto=76.1
    val needleStart = Offset(knobX + 29.1f * scale, knobY + 28.4f * scale)
    val needleEnd = Offset(knobX + 41.0f * scale, knobY + 40.4f * scale)
    drawLine(
        color = VitalColors.KnobInk,
        start = needleStart,
        end = needleEnd,
        strokeWidth = 3.4f * scale,
        cap = StrokeCap.Round
    )

    // Activity selector arc (simplified)
    val arcR = 92.9f * scale
    val spokeLen = 17.9f * scale
    val firstAngle = 16.3f
    val lastAngle = 76.1f
}

/**
 * Draw text elements on the dial
 */
private fun DrawScope.drawTextElements(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    animatedKcal: Float,
    kcalTarget: Float,
    currentPeriod: Period,
    onPeriodChange: (Period) -> Unit,
    accentColor: Color,
    scale: Float,
    width: Float,
    height: Float
) {
    // SUMMARY title (centered at x=215, y=233.5 in du)
    drawText(
        textMeasurer,
        text = "SUMMARY",
        topLeft = Offset(215f * scale, 233.5f * scale),
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 21.5f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.85f.sp
        )
    )

    // MOVE label
    drawText(
        textMeasurer,
        text = "MOVE",
        topLeft = Offset(122f * scale, 312f * scale),
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 22f.sp,
            fontWeight = FontWeight.Medium
        )
    )

    // Calories value
    val calText = "${animatedKcal.toInt()}cal"
    drawText(
        textMeasurer,
        text = calText,
        topLeft = Offset(125f * scale, 402f * scale),
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 60.5f.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = -1.2f.sp
        )
    )

    // SHOW MORE (centered at x=318.25 in du)
    drawText(
        textMeasurer,
        text = "SHOW MORE",
        topLeft = Offset(318.25f * scale, 378f * scale),
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 14f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.2f.sp
        )
    )

    // Time pill text (09:24, centered at x=293.25 in du)
    drawText(
        textMeasurer,
        text = "09:24",
        topLeft = Offset(293.25f * scale, 596.5f * scale),
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 22f.sp,
            fontWeight = FontWeight.Medium
        )
    )

    // KM value
    drawText(
        textMeasurer,
        text = "4.98 km",
        topLeft = Offset(172f * scale, 935f * scale),
        style = TextStyle(
            color = VitalColors.RefWhite,
            fontSize = 28.5f.sp,
            fontWeight = FontWeight.Medium
        )
    )

    // STEPS label
    drawText(
        textMeasurer,
        text = "STEPS",
        topLeft = Offset(440f * scale, 863f * scale),
        style = TextStyle(
            color = VitalColors.RefLabelGray,
            fontSize = 16f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = -1f.sp
        )
    )

    // Steps value
    drawText(
        textMeasurer,
        text = "11.624",
        topLeft = Offset(436f * scale, 947f * scale),
        style = TextStyle(
            color = VitalColors.RefWhite,
            fontSize = 49f.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.8f.sp
        )
    )

    // BPM (centered at x=293.75 in du)
    drawText(
        textMeasurer,
        text = "128bpm",
        topLeft = Offset(293.75f * scale, 1059f * scale),
        style = TextStyle(
            color = VitalColors.RefInk,
            fontSize = 17f.sp,
            fontWeight = FontWeight.Normal
        )
    )

    // ===== CHIPS / PILLS =====
    val strokeWidth1 = 2.6f * scale
    val strokeWidth2 = 1.7f * scale
    val strokeWidth3 = 1.5f * scale

    // Day chip (orange-chip)
    drawRoundRect(
        color = VitalColors.RefOrangeChip,
        topLeft = Offset(122f * scale, 199f * scale),
        size = Size(99f * scale, 41f * scale),
        cornerRadius = CornerRadius(4f * scale)
    )

    // Date chip (orange-chip)
    drawRoundRect(
        color = VitalColors.RefOrangeChip,
        topLeft = Offset(637f * scale, 199f * scale),
        size = Size(99f * scale, 41f * scale),
        cornerRadius = CornerRadius(4f * scale)
    )

    // SHOW MORE pill outline (ink stroke)
    drawRoundRect(
        color = Color.Transparent,
        topLeft = Offset(536f * scale, 341f * scale),
        size = Size(201f * scale, 59f * scale),
        cornerRadius = CornerRadius(7.5f * scale),
        style = Stroke(strokeWidth1)
    )

    // Time pill (gradient)
    val timePillBrush = Brush.verticalGradient(
        0f to Color(0xFF3A4144), // 0%
        0.1f to Color(0xFF5B656C), // 10%
        0.25f to Color(0xFF5F6A72), // 25%
        0.75f to Color(0xFF5E6870), // 75%
        0.9f to Color(0xFF5A646B), // 90%
        1f to Color(0xFF3C4346), // 100%
    )
    drawRoundRect(
        brush = timePillBrush,
        topLeft = Offset(505f * scale, 561f * scale),
        size = Size(163f * scale, 46f * scale),
        cornerRadius = CornerRadius(8.5f * scale)
    )

    // BPM pill (orange)
    drawRoundRect(
        color = VitalColors.RefOrange,
        topLeft = Offset(506f * scale, 1025f * scale),
        size = Size(163f * scale, 48f * scale),
        cornerRadius = CornerRadius(24f * scale)
    )

    // Pointer notch on dial's left edge
    drawPath(
        Path().apply {
            moveTo(122f * scale, 879f * scale)
            lineTo(147.5f * scale, 917f * scale)
            lineTo(122f * scale, 955f * scale)
            close()
        },
        color = VitalColors.RefOrange
    )
    drawPath(
        Path().apply {
            moveTo(121.5f * scale, 899f * scale)
            lineTo(137.5f * scale, 917f * scale)
            lineTo(121.5f * scale, 935.5f * scale)
        },
        color = VitalColors.RefInk,
        style = Stroke(strokeWidth2, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
    )

    // ===== L/M/H PERIOD SWITCH (bottom) =====
    val switchY = height * 0.85f
    val switchHeight = height * 0.12f
    val switchWidth = width * 0.6f
    val switchX = (width - switchWidth) / 2
    val third = switchWidth / 3

    for (i in 0..2) {
        val p = Period.values()[i]
        val x = switchX + i * third
        val isSelected = p == currentPeriod

        val rect = Rect(
            left = x,
            top = switchY,
            right = x + third,
            bottom = switchY + switchHeight
        )

        // Background
        drawRect(
            color = if (isSelected) accentColor else VitalColors.TagButtonFill,
            topLeft = rect.topLeft,
            size = rect.size
        )

        // Border
        drawRect(
            color = if (isSelected) accentColor else VitalColors.NearBlackRowStroke,
            topLeft = rect.topLeft,
            size = rect.size,
            style = Stroke(strokeWidth3)
        )

        // Label
        val label = p.name
        val textColor = if (isSelected) Color.Black else VitalColors.PrimaryText
        drawText(
            textMeasurer,
            text = label,
            topLeft = Offset(
                x + third / 2 - 8 * scale,
                switchY + switchHeight / 2 + 6 * scale
            ),
            style = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
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