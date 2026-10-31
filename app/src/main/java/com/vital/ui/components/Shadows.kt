package com.vital.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePaint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.insetShadow(
    elevation: Dp = 4.dp,
    cornerRadius: Dp = 12.dp,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f)
): Modifier = this.drawBehind {
    val elevationPx = elevation.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    val rect = Rect(
        left = elevationPx,
        top = elevationPx,
        right = size.width - elevationPx,
        bottom = size.height - elevationPx
    )

    // Draw inner shadow using a path with rounded corners
    val path = Path().apply {
        addRoundRect(
            RoundRect(rect, cornerRadiusPx, cornerRadiusPx),
            Path.Direction.Clockwise
        )
    }

    drawIntoCanvas { canvas ->
        val frameworkPaint = android.graphics.Paint().apply {
            color = shadowColor.value.toInt()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = elevationPx * 2
            isAntiAlias = true
            maskFilter = android.graphics.BlurMaskFilter(elevationPx, android.graphics.BlurMaskFilter.Blur.INNER)
        }
        canvas.drawPath(path, frameworkPaint.asComposePaint())
    }
}

// Alternative implementation using RenderEffect for API 31+
@Composable
fun Modifier.insetShadowV2(
    elevation: Dp = 4.dp,
    cornerRadius: Dp = 12.dp,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f)
): Modifier = this.graphicsLayer {
    // RenderEffect.createBlurEffect won't work for inner shadow directly;
    // kept as a graphicsLayer hook for API 31+ effects.
}

// Simple inset shadow using multiple draws for better compatibility
fun Modifier.insetShadowSimple(
    depth: Dp = 2.dp,
    cornerRadius: Dp = 12.dp
): Modifier = this.drawBehind {
    val depthPx = depth.toPx()
    val cornerRadiusPx = cornerRadius.toPx()

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0x40000000), // Shadow start
            Color(0x00000000)  // Shadow end
        ),
        start = Offset(0f, depthPx),
        end = Offset(0f, size.height - depthPx)
    )

    drawRoundRect(
        brush = brush,
        topLeft = Offset(0f, depthPx),
        size = Size(size.width, size.height - depthPx),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}
