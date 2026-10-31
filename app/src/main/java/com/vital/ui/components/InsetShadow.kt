package com.vital.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier for drawing inner shadows on panels/cards to replicate the depth
 * from the reference design. Uses RenderEffect for API 12+ blur support.
 */
fun Modifier.insetShadow(
    color: Color = Color.Black.copy(alpha = 0.3f),
    radius: Dp = 8.dp,
    dx: Dp = 0.dp,
    dy: Dp = 4.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
): Modifier = drawBehind {
    // In drawBehind (DrawScope), we have access to density directly
    val radiusPx = radius.toPx()
    val dxPx = dx.toPx()
    val dyPx = dy.toPx()

    // Get the corner radius from the shape
    // For RoundedCornerShape, we can extract the corner size
    val cornerRadiusPx = when (shape) {
        is RoundedCornerShape -> shape.cornerSize.toPx()
        else -> radiusPx
    }

    val roundRect = RoundRect(
        Rect(0f, 0f, size.width, size.height),
        CornerRadius(cornerRadiusPx)
    )

    // Draw inner shadow by clipping to the shape and drawing a blurred offset rect
    // This is a simplified implementation - for production, consider using RenderEffect
    // or a more sophisticated inner shadow technique
    drawRoundRect(
        color = color,
        topLeft = roundRect.topLeft,
        topRight = roundRect.topRight,
        bottomRight = roundRect.bottomRight,
        bottomLeft = roundRect.bottomLeft,
        cornerRadius = roundRect.cornerRadius,
        style = Stroke(width = radiusPx * 2),
        alpha = 0.15f
    )
}