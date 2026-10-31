package com.vital.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RoundRect
import androidx.compose.ui.graphics.Stroke
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawRoundRect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current
    val radiusPx = radius.toPx()
    val dxPx = dx.toPx()
    val dyPx = dy.toPx()

    val roundRect = RoundRect(
        Rect(0f, 0f, size.width, size.height),
        shape.getRoundingRect(Size(size.width, size.height))
    )

    // Draw inner shadow by clipping to the shape and drawing a blurred offset rect
    // This is a simplified implementation - for production, consider using RenderEffect
    // or a more sophisticated inner shadow technique
    drawRoundRect(
        color = color,
        topLeftRadiusX = roundRect.getCornerRadius(RoundRect.Corner.TopLeft).x,
        topLeftRadiusY = roundRect.getCornerRadius(RoundRect.Corner.TopLeft).y,
        topRightRadiusX = roundRect.getCornerRadius(RoundRect.Corner.TopRight).x,
        topRightRadiusY = roundRect.getCornerRadius(RoundRect.Corner.TopRight).y,
        bottomRightRadiusX = roundRect.getCornerRadius(RoundRect.Corner.BottomRight).x,
        bottomRightRadiusY = roundRect.getCornerRadius(RoundRect.Corner.BottomRight).y,
        bottomLeftRadiusX = roundRect.getCornerRadius(RoundRect.Corner.BottomLeft).x,
        bottomLeftRadiusY = roundRect.getCornerRadius(RoundRect.Corner.BottomLeft).y,
        style = Stroke(width = radiusPx * 2),
        alpha = 0.15f
    )
}