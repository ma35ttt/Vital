package com.vital.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Inner shadow using a gradient (works in any DrawScope context).
 * Draws a top/left inner shadow by drawing an inset rounded rect with a fade-out gradient.
 * Usage: Modifier.insetShadow(elevation = 4.dp, cornerRadius = 12.dp)
 */
fun Modifier.insetShadow(
    elevation: Dp = 4.dp,
    cornerRadius: Dp = 12.dp,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f)
): Modifier = this.drawBehind {
    val elevationPx = elevation.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    // Draw top inner shadow (fade from top edge inward)
    val topBrush = Brush.linearGradient(
        colors = listOf(
            shadowColor.copy(alpha = 0.25f),
            shadowColor.copy(alpha = 0f)
        ),
        start = Offset(0f, elevationPx),
        end = Offset(0f, elevationPx * 3)
    )
    drawRoundRect(
        brush = topBrush,
        topLeft = Offset(elevationPx, elevationPx),
        size = Size(size.width - elevationPx * 2, elevationPx * 2),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )

    // Draw left inner shadow (fade from left edge inward)
    val leftBrush = Brush.linearGradient(
        colors = listOf(
            shadowColor.copy(alpha = 0.25f),
            shadowColor.copy(alpha = 0f)
        ),
        start = Offset(elevationPx, 0f),
        end = Offset(elevationPx * 3, 0f)
    )
    drawRoundRect(
        brush = leftBrush,
        topLeft = Offset(elevationPx, elevationPx),
        size = Size(elevationPx * 2, size.height - elevationPx * 2),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}
