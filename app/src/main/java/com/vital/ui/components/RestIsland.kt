package com.vital.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.ui.formatRestTime
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts

/** The island's own black stays a cutout in every theme, like the live-island mock. */
private val IslandFill = Color(0xFF050505)
private val IslandInk = Color(0xFFF3F1ED)
private val IslandMuted = Color(0xFF8F8A83)
private val IslandTrack = Color(0xFF2A2724)

/**
 * Compact live-rest pill, centred in the status-bar row like the dynamic-island mock.
 * The ring drains with the remaining rest and the orange dot pulses to say it is live.
 * Tapping it opens Lift, where the rest controls live.
 */
@Composable
fun RestIsland(
    remainingSeconds: Int,
    totalSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds <= 0) {
        0f
    } else {
        (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    }
    val pulse by rememberInfiniteTransition(label = "restPulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "restPulseAlpha"
    )

    Row(
        modifier = modifier
            .height(38.dp)
            .background(IslandFill, RoundedCornerShape(999.dp))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val stroke = 2.5.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = IslandTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )
            if (progress > 0f) {
                drawArc(
                    color = VitalColors.orange,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = "Rest",
                color = IslandInk,
                fontFamily = VitalFonts.JetBrainsMonoMedium,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.01.em
            )
            Text(
                text = formatRestTime(remainingSeconds),
                color = IslandMuted,
                fontFamily = VitalFonts.JetBrainsMonoMedium,
                fontSize = 11.sp,
                letterSpacing = 0.01.em
            )
        }
        Box(
            modifier = Modifier
                .graphicsLayer {
                    alpha = pulse
                    scaleX = 0.85f + pulse * 0.3f
                    scaleY = 0.85f + pulse * 0.3f
                }
                .size(6.dp)
                .background(VitalColors.orange, CircleShape)
        )
    }
}