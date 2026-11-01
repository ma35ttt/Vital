package com.vital.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier for drawing inner shadows on panels/cards to replicate the depth
 * from the reference design. Uses a simple stroke inset approach.
 */
fun Modifier.insetShadow(
    color: Color = Color.Black.copy(alpha = 0.3f),
    radius: Dp = 8.dp,
    dx: Dp = 0.dp,
    dy: Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
): Modifier = drawBehind {
    val radiusPx = radius.toPx()
    val dxPx = dx.toPx()
    val dyPx = dy.toPx()

    // Extract corner radius from shape (assume uniform for RoundedCornerShape)
    val cornerRadiusPx = when (shape) {
        is RoundedCornerShape -> {
            // RoundedCornerShape stores corner size internally; approximate from radius param
            // For 16.dp shape corner, use a fixed conversion
            16.dp.toPx()
        }
        else -> radiusPx
    }

    val cornerRadius = CornerRadius(cornerRadiusPx)

    // Draw inner shadow by drawing a slightly inset rounded rect with stroke
    drawRoundRect(
        color = color,
        topLeft = Offset(dxPx, dyPx),
        size = Size(size.width - dxPx * 2, size.height - dyPx * 2),
        cornerRadius = cornerRadius,
        style = Stroke(width = radiusPx * 2),
        alpha = 0.15f
    )
}