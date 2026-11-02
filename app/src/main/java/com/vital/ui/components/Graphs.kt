package com.vital.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.LocalDesignUnitsScale
import kotlin.math.max
import kotlin.math.min
import android.graphics.BlurMaskFilter

@Composable
fun HeroLineGraph(
    modifier: Modifier = Modifier,
    dataPoints: List<Float>, // Normalized 0-1 values
    labels: List<String> = listOf("6m", "7m", "8m", "9m", "10m", "11m", "12m", "13m"),
    accentColor: androidx.compose.ui.graphics.Color = com.vital.ui.theme.VitalColors.RefInk, // black polyline
    showTooltip: Boolean = false,
    tooltipIndex: Int = -1,
    tooltipValue: Float = 0f,
    tooltipLabel: String = "BPM"
) {
    val textMeasurer = rememberTextMeasurer()
    // Read design scale once for use in DrawScope (cannot call @Composable inside DrawScope)
    val duScale = LocalDesignUnitsScale.current
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width.toFloat()
        val height = size.height.toFloat()

        // Design units: 393×855 frame, scale = screenWidthDp / 393
        // Panel: 6 du inset from card edges, radius ~6 du
        // FLAT #5F6971, NOT gradient
        // Inner shadow top/left edges ~#424143 fading over ~7 du (real blur)
        // Guide lines at 15% and 50% panel height, 1.8 du thick, darker than panel
        // Polyline stops at end dot (85.6% panel width, 53% panel height)
        // End dot: 6 du radius
        // Tooltip pill ~55 x 19 du, centred on guide line 1 y and end dot x, thin outline ONLY, text "111BMP"
        // Vertical leader from pill to dot
        // Bottom labels at ~87% panel height: "6m — 7m — 8m — 9m — 10m — 11m — 12m — 13m", tiny ~5-6 du

        val panelInset = 0f // Inset already applied by parent Surface padding
        val panelRadius = 6f * duScale
        val panelLeft = panelInset
        val panelTop = panelInset
        val panelRight = width - panelInset
        val panelBottom = height - panelInset
        val panelWidth = panelRight - panelLeft
        val panelHeight = panelBottom - panelTop

        // Handle empty data — draw panel & guides but no polyline, end dot, or tooltip
        if (dataPoints.isEmpty()) {
            val panelRect = Rect(
                left = panelLeft,
                top = panelTop,
                right = panelRight,
                bottom = panelBottom
            )
            drawRoundRect(
                color = com.vital.ui.theme.VitalColors.panelGrey,
                topLeft = panelRect.topLeft,
                size = panelRect.size,
                cornerRadius = CornerRadius(panelRadius, panelRadius)
            )
            // Guide lines
            val guideLineY1 = panelTop + panelHeight * 0.15f
            val guideLineY2 = panelTop + panelHeight * 0.50f
            val guideLineColor = com.vital.ui.theme.VitalColors.InsetGreyGuideLine
            val guideLineThickness = 1.8f * duScale
            for (y in listOf(guideLineY1, guideLineY2)) {
                drawLine(
                    color = guideLineColor,
                    start = Offset(panelLeft, y),
                    end = Offset(panelRight, y),
                    strokeWidth = guideLineThickness
                )
            }
            // Bottom labels
            val labelY = panelTop + panelHeight * 0.87f
            val labelFontSize = 5.5f * duScale
            val numLabels = labels.size
            if (numLabels > 1) {
                for (i in 0 until numLabels) {
                    val x = panelLeft + (i / (numLabels - 1).toFloat()) * panelWidth
                    val labelText = if (i == numLabels - 1) labels[i] else "${labels[i]} —"
                    drawText(
                        textMeasurer,
                        text = labelText,
                        topLeft = Offset(x - 8f * duScale, labelY),
                        style = TextStyle(
                            fontSize = (labelFontSize).sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = com.vital.ui.theme.VitalColors.InsetGreyBottomLabels
                        )
                    )
                }
            }
            return@Canvas
        }

        // Normalize data so last point maps to 53% panel height (endDotYRatio)
        val lastValue = dataPoints.last()
        val maxValue = dataPoints.maxOrNull() ?: 1f
        val minValue = dataPoints.minOrNull() ?: 0f
        val dataRange = (maxValue - minValue).coerceAtLeast(0.001f)

        // ===== INSET GREY PANEL BACKGROUND (FLAT #5F6971, NOT gradient) =====
        val panelRect = Rect(
            left = panelLeft,
            top = panelTop,
            right = panelRight,
            bottom = panelBottom
        )
        drawRoundRect(
            color = com.vital.ui.theme.VitalColors.panelGrey, // #5F6971 flat
            topLeft = panelRect.topLeft,
            size = panelRect.size,
            cornerRadius = CornerRadius(panelRadius, panelRadius)
        )

        // ===== INNER SHADOW on top/left edges (~#424143 fading over ~7 du) =====
        // Use real blur via BlurMaskFilter on a path for authentic inner shadow
        val shadowDepth = 7f * duScale
        val shadowColor = com.vital.ui.theme.VitalColors.panelGreyShadow // #424143
        val shadowPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(180,
                (shadowColor.red * 255).toInt(),
                (shadowColor.green * 255).toInt(),
                (shadowColor.blue * 255).toInt())
            maskFilter = BlurMaskFilter(shadowDepth, BlurMaskFilter.Blur.NORMAL)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = shadowDepth * 2
        }

        // Top inner shadow: draw horizontal line at top edge, blurred
        drawIntoCanvas { canvas ->
            val path = android.graphics.Path().apply {
                moveTo(panelLeft, panelTop + panelRadius)
                lineTo(panelRight, panelTop + panelRadius)
            }
            canvas.nativeCanvas.drawPath(path, shadowPaint)
        }

        // Left inner shadow: draw vertical line at left edge, blurred
        drawIntoCanvas { canvas ->
            val path = android.graphics.Path().apply {
                moveTo(panelLeft + panelRadius, panelTop)
                lineTo(panelLeft + panelRadius, panelBottom)
            }
            canvas.nativeCanvas.drawPath(path, shadowPaint)
        }

        // ===== TWO HORIZONTAL GUIDE LINES at 15% and 50% panel height =====
        // 1.8 du thick, darker than panel
        val guideLineY1 = panelTop + panelHeight * 0.15f
        val guideLineY2 = panelTop + panelHeight * 0.50f
        val guideLineColor = com.vital.ui.theme.VitalColors.InsetGreyGuideLine // #4A535A
        val guideLineThickness = 1.8f * duScale

        for (y in listOf(guideLineY1, guideLineY2)) {
            drawLine(
                color = guideLineColor,
                start = Offset(panelLeft, y),
                end = Offset(panelRight, y),
                strokeWidth = guideLineThickness
            )
        }

        // ===== BOTTOM LABELS "6m — 7m — 8m — 9m — 10m — 11m — 12m — 13m" with dashes =====
        // At ~87% panel height, tiny ~5-6 du font
        val labelY = panelTop + panelHeight * 0.87f
        val labelFontSize = 5.5f * duScale
        val numLabels = labels.size
        if (numLabels > 1) {
            for (i in 0 until numLabels) {
                val x = panelLeft + (i / (numLabels - 1).toFloat()) * panelWidth
                val labelText = if (i == numLabels - 1) labels[i] else "${labels[i]} —"
                drawText(
                    textMeasurer,
                    text = labelText,
                    topLeft = Offset(x - 8f * duScale, labelY),
                    style = TextStyle(
                        fontSize = (labelFontSize).sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = com.vital.ui.theme.VitalColors.InsetGreyBottomLabels
                    )
                )
            }
        }

        // ===== BLACK POLYLINE - STOPS at end dot (85.6% panel width, 53% panel height) =====
        // Map data so last point is at 53% panel height (endDotYRatio)
        val numPoints = dataPoints.size
        val endDotXRatio = 0.856f // 85.6% panel width
        val endDotYRatio = 0.53f  // 53% panel height

        val path = Path().apply {
            for (i in dataPoints.indices) {
                // Map data point index to x position (stopping at endDotXRatio)
                val xRatio = (i / (numPoints - 1).toFloat()) * endDotXRatio
                val x = panelLeft + xRatio * panelWidth
                // Normalize data: last value maps to 0 (53% height), max maps to 1 (top)
                val normalizedValue = (dataPoints[i] - lastValue) / dataRange
                val y = panelTop + panelHeight * endDotYRatio * (1f - normalizedValue.coerceIn(0f, 1f))
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        // Shadow path (offset down by 2px)
        val shadowPath = Path().apply {
            for (i in dataPoints.indices) {
                val xRatio = (i / (numPoints - 1).toFloat()) * endDotXRatio
                val x = panelLeft + xRatio * panelWidth
                val normalizedValue = (dataPoints[i] - lastValue) / dataRange
                val y = panelTop + panelHeight * endDotYRatio * (1f - normalizedValue.coerceIn(0f, 1f)) + 2.dp.toPx()
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb((0.3f * 255f).toInt(), 0, 0, 0)
                maskFilter = BlurMaskFilter(2.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2.5.dp.toPx()
                strokeCap = android.graphics.Paint.Cap.ROUND
            }
            canvas.nativeCanvas.drawPath(shadowPath.asAndroidPath(), paint)
        }

        // Main black polyline (RefInk #070707)
        drawPath(
            path,
            color = accentColor, // black (RefInk)
            style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // ===== END DOT at 85.6% panel width, 53% panel height =====
        // End dot fixed at 53% panel height (last normalized value = 0)
        val lastIndex = dataPoints.lastIndex
        val lastX = panelLeft + endDotXRatio * panelWidth
        val lastY = panelTop + panelHeight * endDotYRatio // Fixed at 53% panel height

        val endDotRadius = 6f * duScale
        val endDotInnerRadius = 2.5f * duScale

        // Only show end dot and tooltip when data exists (showTooltip)
        if (showTooltip) {
            // Outer dot (black)
            drawCircle(
                color = accentColor,
                center = Offset(lastX, lastY),
                radius = endDotRadius
            )
            // White inner dot
            drawCircle(
                color = Color.White,
                center = Offset(lastX, lastY),
                radius = endDotInnerRadius
            )

            // ===== TOOLTIP PILL centred on guide line 1 y AND end dot x =====
            // ~55 x 19 du, centred on guide line 1 and on the end dot x, thin outline ONLY, text "111BMP"
            val tooltipX = lastX // centred on end dot x
            val tooltipY = guideLineY1 // centred on guide line 1 y

            val tooltipText = "${tooltipValue.toInt()}$tooltipLabel"
            val tooltipStyle = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            val layoutResult = textMeasurer.measure(tooltipText, tooltipStyle)
            val textWidth = layoutResult.size.width
            // Pill size ~55 x 19 du
            val tooltipWidth = 55f.du(duScale)
            val tooltipHeight = 19f.du(duScale)
            val tooltipPaddingH = (tooltipWidth - textWidth) / 2
            val tooltipPaddingV = 2f.du(duScale)

            val bubbleRect = Rect(
                left = tooltipX - tooltipWidth / 2,
                top = tooltipY - tooltipHeight / 2,
                right = tooltipX + tooltipWidth / 2,
                bottom = tooltipY + tooltipHeight / 2
            )

            // Pill outline ONLY (thin, accent color), no fill background
            drawRoundRect(
                color = com.vital.ui.theme.VitalColors.orange,
                topLeft = bubbleRect.topLeft,
                size = bubbleRect.size,
                cornerRadius = CornerRadius(bubbleRect.height / 2f),
                style = Stroke(1.dp.toPx())
            )

            // Pill text
            drawText(
                layoutResult,
                topLeft = Offset(tooltipX - textWidth / 2, tooltipY - layoutResult.firstBaseline / 2f)
            )

            // Vertical leader from pill bottom to end dot
            // Thin, guide line color
            val leaderYStart = bubbleRect.bottom
            val leaderYEnd = lastY
            if (leaderYEnd > leaderYStart) {
                drawLine(
                    color = guideLineColor.copy(alpha = 0.7f),
                    start = Offset(tooltipX, leaderYStart),
                    end = Offset(tooltipX, leaderYEnd),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// DesignUnits extension for .du() in DrawScope - uses captured duScale (NOT @Composable)
private fun Float.du(duScale: Float): Float = this * duScale

private fun Int.du(duScale: Float): Float = this * duScale

@Composable
fun Sparkline(
    modifier: Modifier = Modifier,
    dataPoints: List<Float>,
    accentColor: androidx.compose.ui.graphics.Color,
    dateLabel: String = "",
    rulerLabels: List<String> = listOf("960", "980", "990"), // Numbers, NOT MIN AVG MAX
    activeRulerIndex: Int = 1, // Middle (980) active = dark
    width: Float = 0.28f, // Fraction of parent width (per SPEC: 0.28W x 0.185W)
    height: Float = 0.185f // Fraction of parent height
) {
    val textMeasurer = rememberTextMeasurer()
    // Read design scale once for use in DrawScope (cannot call @Composable inside DrawScope)
    val duScale = LocalDesignUnitsScale.current
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(width / height)
    ) {
        val w = size.width.toFloat()
        val h = size.height.toFloat()

        // Thumbnail: 108.7×73 du, flat #5F6971
        val panelColor = com.vital.ui.theme.VitalColors.panelGrey // #5F6971 flat
        val padding = 4f.du(duScale)
        val topPadding = 14f.du(duScale) // space for date label
        val bottomPadding = 18f.du(duScale) // space for ruler
        val graphWidth = w - 2 * padding
        val graphHeight = h - topPadding - bottomPadding - 2 * padding

        if (dataPoints.size < 2) return@Canvas

        // ===== BACKGROUND PANEL (flat #5F6971) =====
        drawRect(
            color = panelColor,
            topLeft = Offset.Zero,
            size = Size(w, h)
        )

        // ===== DATE LABEL (top-left) =====
        if (dateLabel.isNotBlank()) {
            val dateStyle = TextStyle(
                fontSize = (7f.du(duScale)).sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = com.vital.ui.theme.VitalColors.InsetGreyBottomLabels
            )
            drawText(
                textMeasurer,
                text = dateLabel,
                topLeft = Offset(padding, padding + 8f.du(duScale)),
                style = dateStyle
            )
        }

        // ===== SPARKLINE POLYLINE =====
        val path = Path().apply {
            for (i in dataPoints.indices) {
                val x = padding + (i / (dataPoints.size - 1).toFloat()) * graphWidth
                val y = topPadding + padding + graphHeight - dataPoints[i] * graphHeight
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        drawPath(
            path,
            color = accentColor,
            style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // ===== END DOT WITH VERTICAL LEADER =====
        val lastIndex = dataPoints.lastIndex
        val lastX = padding + (lastIndex / (dataPoints.size - 1).toFloat()) * graphWidth
        val lastY = topPadding + padding + graphHeight - dataPoints[lastIndex] * graphHeight

        // Vertical leader from end dot to bottom of graph area
        drawLine(
            color = com.vital.ui.theme.VitalColors.InsetGreyGuideLine.copy(alpha = 0.5f),
            start = Offset(lastX, lastY),
            end = Offset(lastX, topPadding + padding + graphHeight),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        // End dot (accent color)
        drawCircle(
            color = accentColor,
            center = Offset(lastX, lastY),
            radius = 3.5.dp.toPx()
        )
        // White inner dot
        drawCircle(
            color = Color.White,
            center = Offset(lastX, lastY),
            radius = 1.5.dp.toPx()
        )

        // ===== BOTTOM RULER with numbers (960/980/990), active dark =====
        val rulerY = topPadding + padding + graphHeight + 4f.du(duScale)
        val rulerCount = rulerLabels.size
        for (i in rulerLabels.indices) {
            val x = padding + (i / (rulerCount - 1).toFloat()) * graphWidth
            val isActive = i == activeRulerIndex
            // Active = dark (textPrimary), inactive = lighter (InsetGreyBottomLabels)
            val labelColor = if (isActive) {
                com.vital.ui.theme.VitalColors.textPrimary
            } else {
                com.vital.ui.theme.VitalColors.InsetGreyBottomLabels
            }
            // Tiny tick mark
            drawLine(
                color = labelColor,
                start = Offset(x, rulerY - 2f.du(duScale)),
                end = Offset(x, rulerY + 1f.du(duScale)),
                strokeWidth = 0.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Label
            val labelStyle = TextStyle(
                fontSize = (6f.du(duScale)).sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            val layoutResult = textMeasurer.measure(rulerLabels[i], labelStyle)
            drawText(
                layoutResult,
                topLeft = Offset(x - layoutResult.size.width / 2f, rulerY + 2f.du(duScale))
            )
        }
    }
}

@Composable
@Preview
fun HeroLineGraphPreview() {
    val data = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val labels = listOf("6m", "7m", "8m", "9m", "10m", "11m", "12m", "13m")

    Surface(
        modifier = Modifier.size(393.dp, 150.dp),
        color = com.vital.ui.theme.VitalColors.Background
    ) {
        HeroLineGraph(
            modifier = Modifier.fillMaxSize(),
            dataPoints = data,
            labels = labels,
            accentColor = com.vital.ui.theme.VitalColors.RefInk,
            showTooltip = true,
            tooltipIndex = 4,
            tooltipValue = 111f
        )
    }
}

@Composable
@Preview
fun SparklinePreview() {
    val data = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)

    Surface(
        modifier = Modifier.size(110.dp, 73.dp),
        color = com.vital.ui.theme.VitalColors.Background
    ) {
        Sparkline(
            modifier = Modifier.fillMaxSize(),
            dataPoints = data,
            accentColor = com.vital.ui.theme.VitalColors.orange,
            dateLabel = "OCT 31",
            rulerLabels = listOf("960", "980", "990"),
            activeRulerIndex = 1
        )
    }
}