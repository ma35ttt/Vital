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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun HeroLineGraph(
    modifier: Modifier = Modifier,
    dataPoints: List<Float>, // Normalized 0-1 values
    labels: List<String> = emptyList(), // Day labels (MON, TUE, etc.)
    accentColor: androidx.compose.ui.graphics.Color,
    showTooltip: Boolean = false,
    tooltipIndex: Int = -1,
    tooltipValue: Float = 0f
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width.toFloat()
        val height = size.height.toFloat()
        val padding = 12.dp.toPx()
        val labelHeight = 18.dp.toPx() // reserved space for labels at bottom
        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding - labelHeight

        if (dataPoints.isEmpty()) return@Canvas

        // ===== INSET GREY PANEL BACKGROUND =====
        val panelRect = Rect(
            left = padding,
            top = padding,
            right = width - padding,
            bottom = height - padding
        )
        drawRect(
            color = com.vital.ui.theme.VitalColors.InsetGreyPanel.copy(alpha = 0.15f),
            topLeft = panelRect.topLeft,
            size = panelRect.size
        )

        // Horizontal grid lines (5 lines) - within graph area only
        for (i in 0..4) {
            val y = padding + (i / 4f) * graphHeight
            drawLine(
                color = com.vital.ui.theme.VitalColors.InsetGreyGridLines,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        // Vertical grid lines (based on data points)
        val numPoints = dataPoints.size
        for (i in 0 until numPoints) {
            val x = padding + (i / (numPoints - 1).toFloat()) * graphWidth
            drawLine(
                color = com.vital.ui.theme.VitalColors.InsetGreyGridLines,
                start = Offset(x, padding),
                end = Offset(x, padding + graphHeight),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        // Day labels at bottom - inside the reserved label area
        if (labels.size == numPoints) {
            for (i in 0 until numPoints) {
                val x = padding + (i / (numPoints - 1).toFloat()) * graphWidth
                drawText(
                    textMeasurer,
                    text = labels[i],
                    topLeft = Offset(x - 12.dp.toPx(), padding + graphHeight + 4.dp.toPx()),
                    style = TextStyle(color = com.vital.ui.theme.VitalColors.InsetGreyLabels, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                )
            }
        }

        // ===== POLYLINE =====
        val path = Path().apply {
            for (i in dataPoints.indices) {
                val x = padding + (i / (numPoints - 1).toFloat()) * graphWidth
                val y = padding + graphHeight - dataPoints[i] * graphHeight
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
            style = Stroke(2.5.dp.toPx())
        )

        // ===== END DOT =====
        val lastIndex = dataPoints.lastIndex
        val lastX = padding + (lastIndex / (numPoints - 1).toFloat()) * graphWidth
        val lastY = padding + graphHeight - dataPoints[lastIndex] * graphHeight
        drawCircle(
            color = accentColor,
            center = Offset(lastX, lastY),
            radius = 5.dp.toPx()
        )

        // White inner dot
        drawCircle(
            color = Color.White,
            center = Offset(lastX, lastY),
            radius = 2.dp.toPx()
        )

        // ===== TOOLTIP BUBBLE =====
        if (showTooltip && tooltipIndex >= 0 && tooltipIndex < numPoints) {
            val tooltipX = padding + (tooltipIndex / (numPoints - 1).toFloat()) * graphWidth
            val tooltipY = padding + graphHeight - dataPoints[tooltipIndex] * graphHeight

            val tooltipText = tooltipValue.toInt().toString()
            val textWidth = tooltipText.length * 8.dp.toPx()
            val tooltipPadding = 8.dp.toPx()

            val bubbleRect = Rect(
                left = tooltipX - textWidth / 2 - tooltipPadding,
                top = tooltipY - 30.dp.toPx(),
                right = tooltipX + textWidth / 2 + tooltipPadding,
                bottom = tooltipY - 8.dp.toPx()
            )

            // Bubble background
            drawRoundRect(
                color = com.vital.ui.theme.VitalColors.Background,
                topLeft = bubbleRect.topLeft,
                size = bubbleRect.size,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Bubble border
            drawRoundRect(
                color = accentColor,
                topLeft = bubbleRect.topLeft,
                size = bubbleRect.size,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(1.5.dp.toPx())
            )

            // Bubble text
            drawText(
                textMeasurer,
                text = tooltipText,
                topLeft = Offset(tooltipX - textWidth / 2, tooltipY - 16.dp.toPx()),
                style = TextStyle(color = com.vital.ui.theme.VitalColors.PrimaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )

            // Arrow pointing down
            val arrowPath = Path().apply {
                moveTo(tooltipX, tooltipY - 8.dp.toPx())
                lineTo(tooltipX - 6.dp.toPx(), tooltipY - 16.dp.toPx())
                lineTo(tooltipX + 6.dp.toPx(), tooltipY - 16.dp.toPx())
                close()
            }
            drawPath(
                arrowPath,
                color = com.vital.ui.theme.VitalColors.Background
            )
            drawPath(
                arrowPath,
                color = accentColor,
                style = Stroke(1.5.dp.toPx())
            )
        }
    }
}

@Composable
fun Sparkline(
    modifier: Modifier = Modifier,
    dataPoints: List<Float>,
    accentColor: androidx.compose.ui.graphics.Color,
    width: Float = 0.28f, // Fraction of parent width
    height: Float = 0.185f // Fraction of parent height
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(width / height)
    ) {
        val w = size.width.toFloat()
        val h = size.height.toFloat()
        val padding = 4.dp.toPx()
        val graphWidth = w - 2 * padding
        val graphHeight = h - 2 * padding

        if (dataPoints.size < 2) return@Canvas

        val path = Path().apply {
            for (i in dataPoints.indices) {
                val x = padding + (i / (dataPoints.size - 1).toFloat()) * graphWidth
                val y = padding + graphHeight - dataPoints[i] * graphHeight
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
            style = Stroke(1.5.dp.toPx())
        )

        // End dot
        val lastIndex = dataPoints.lastIndex
        val lastX = padding + (lastIndex / (dataPoints.size - 1).toFloat()) * graphWidth
        val lastY = padding + graphHeight - dataPoints[lastIndex] * graphHeight
        drawCircle(
            color = accentColor,
            center = Offset(lastX, lastY),
            radius = 3.dp.toPx()
        )
    }
}

@Composable
@Preview
fun HeroLineGraphPreview() {
    val data = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)
    val labels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    Surface(
        modifier = Modifier.size(300.dp, 180.dp),
        color = com.vital.ui.theme.VitalColors.Background
    ) {
        HeroLineGraph(
            modifier = Modifier.fillMaxSize(),
            dataPoints = data,
            labels = labels,
            accentColor = com.vital.ui.theme.VitalColors.AccentTDY,
            showTooltip = true,
            tooltipIndex = 4,
            tooltipValue = 0.52f
        )
    }
}

@Composable
@Preview
fun SparklinePreview() {
    val data = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f)

    Surface(
        modifier = Modifier.size(100.dp, 60.dp),
        color = com.vital.ui.theme.VitalColors.Background
    ) {
        Sparkline(
            modifier = Modifier.fillMaxSize(),
            dataPoints = data,
            accentColor = com.vital.ui.theme.VitalColors.AccentTDY
        )
    }
}