package com.vital.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Stroke
import androidx.compose.ui.graphics.drawscope.drawCircle
import androidx.compose.ui.graphics.drawscope.drawLine
import androidx.compose.ui.graphics.drawscope.drawPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.px
import com.vital.ui.theme.VitalColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Knurled knob component for the Summary Dial.
 * - Radius: 0.18W (18% of canvas width)
 * - Center: (0.23W, 0.345H)
 * - 1.5dp black outline, serrated edge (tick every 6°)
 * - Inner bevel gradient, black center dot, thick hand pointing at current progress
 * - Dragging rotates knob & updates values live
 */
@Composable
fun KnurledKnob(
    modifier: Modifier = Modifier,
    progress: Float, // 0f to 1f
    onProgressChange: (Float) -> Unit,
    accentColor: Color = VitalColors.DialKnobHand,
    enabled: Boolean = true
) {
    var knobAngle by remember { mutableStateOf(progress * 255f + 150f) } // 150° to 405° (which is 45°)
    val lastAngle = remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .size(120.dp) // Default size, can be overridden by modifier
            .pointerInput(Unit) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { lastAngle.value = knobAngle },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // We'll get the center from the canvas size in the draw phase
                            // For drag, we use the current modifier size
                            val centerX = 60f // 120dp / 2 default
                            val centerY = 60f
                            val newAngle = kotlin.math.atan2(
                                dragAmount.y,
                                dragAmount.x
                            ) * 180f / PI.toFloat()
                            var adjustedAngle = when {
                                newAngle < 0 -> newAngle + 360f
                                else -> newAngle
                            }
                            // Clamp to 150°-405° (which maps to 150°-360° and 0°-45°)
                            if (adjustedAngle > 180f && adjustedAngle < 270f) {
                                // Don't allow this range - clamp to nearest boundary
                                if (Math.abs(adjustedAngle - 180f) < Math.abs(adjustedAngle - 270f)) {
                                    adjustedAngle = 180f
                                } else {
                                    adjustedAngle = 270f
                                }
                            }
                            // Clamp to valid range: 150° to 405° (150° to 360° + 0° to 45°)
                            adjustedAngle = kotlin.math.max(150f, kotlin.math.min(405f, adjustedAngle))
                            knobAngle = adjustedAngle
                            val newProgress = (adjustedAngle - 150f) / 255f
                            onProgressChange(newProgress.coerceIn(0f, 1f))
                        }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = min(size.width, size.height) / 2f
            val handLength = radius * 0.85f

            // Draw knurled edge (serrated - tick every 6°)
            val knurlInnerRadius = radius * 0.92f
            val knurlOuterRadius = radius

            for (i in 0..59) { // 60 ticks = 6° each
                val angle = (i * 6f) * PI.toFloat() / 180f
                val x1 = centerX + cos(angle) * knurlInnerRadius
                val y1 = centerY + sin(angle) * knurlInnerRadius
                val x2 = centerX + cos(angle) * knurlOuterRadius
                val y2 = centerY + sin(angle) * knurlOuterRadius
                drawLine(
                    color = VitalColors.DialKnobOutline,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 1.5f
                )
            }

            // Main knob circle (black outline)
            drawCircle(
                color = Color.Transparent,
                center = Offset(centerX, centerY),
                radius = radius * 0.95f,
                style = Stroke(1.5f, cap = Stroke.Cap.Round, join = Stroke.Join.Round)
            )

            // Inner bevel - lighter arc on top-left
            val bevelPath = Path()
            val bevelRadius = radius * 0.85f
            for (i in 0..90) { // 90° arc on top-left (225° to 315° in standard coords)
                val angle = (225f + i) * PI.toFloat() / 180f
                if (i == 0) {
                    bevelPath.moveTo(
                        centerX + cos(angle) * bevelRadius,
                        centerY + sin(angle) * bevelRadius
                    )
                } else {
                    bevelPath.lineTo(
                        centerX + cos(angle) * bevelRadius,
                        centerY + sin(angle) * bevelRadius
                    )
                }
            }
            drawPath(
                bevelPath,
                color = VitalColors.DialKnobBevelLight.copy(alpha = 0.3f),
                style = Stroke(2f)
            )

            // Inner bevel - darker arc on bottom-right
            val bevelPath2 = Path()
            for (i in 0..90) {
                val angle = (45f + i) * PI.toFloat() / 180f
                if (i == 0) {
                    bevelPath2.moveTo(
                        centerX + cos(angle) * bevelRadius,
                        centerY + sin(angle) * bevelRadius
                    )
                } else {
                    bevelPath2.lineTo(
                        centerX + cos(angle) * bevelRadius,
                        centerY + sin(angle) * bevelRadius
                    )
                }
            }
            drawPath(
                bevelPath2,
                color = VitalColors.DialKnobBevelDark.copy(alpha = 0.5f),
                style = Stroke(2f)
            )

            // Center dot
            drawCircle(
                color = VitalColors.DialKnobCenterDot,
                center = Offset(centerX, centerY),
                radius = radius * 0.12f
            )

            // Hand/pointer - rotates with progress
            rotate(knobAngle) {
                drawLine(
                    color = accentColor,
                    start = Offset(centerX, centerY),
                    end = Offset(
                        centerX + cos(0f) * handLength,
                        centerY + sin(0f) * handLength
                    ),
                    strokeWidth = 4f,
                    cap = Stroke.Cap.Round
                )

                // Hand tip
                drawCircle(
                    color = accentColor,
                    center = Offset(
                        centerX + cos(0f) * (handLength + 4f),
                        centerY + sin(0f) * (handLength + 4f)
                    ),
                    radius = 6f
                )
            }
        }
    }
}