package com.vital.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts

/** 5-tab shell: Meals · Lift · Today · Cardio · More. Today is the centre and the start. */
data class VitalTab(val label: String, val route: String)

val VITAL_TABS = listOf(
    VitalTab("Meals", "meals"),
    VitalTab("Lift", "lift"),
    VitalTab("Today", "today"),
    VitalTab("Cardio", "cardio"),
    VitalTab("More", "more")
)

const val NAV_ROW_HEIGHT_DP = 64

@Composable
fun VitalBottomNav(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showMoreDot: Boolean = false,
    restProgress: Float? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VitalColors.paper)
            .navigationBarsPadding()
    ) {
        RestIndicator(progress = restProgress)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(NAV_ROW_HEIGHT_DP.dp)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VITAL_TABS.forEachIndexed { index, tab ->
                val active = index == selectedIndex
                val color by animateColorAsState(
                    targetValue = if (active) VitalColors.orange else VitalColors.navInactive,
                    animationSpec = tween(durationMillis = 220),
                    label = "navColor$index"
                )
                val glyphScale by animateFloatAsState(
                    targetValue = if (active) 1f else 0.88f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                    label = "navScale$index"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box {
                        VitalNavGlyph(
                            tabIndex = index,
                            color = color,
                            modifier = Modifier
                                .size(21.dp)
                                .graphicsLayer {
                                    scaleX = glyphScale
                                    scaleY = glyphScale
                                }
                        )
                        // Quiet 8 dp "something needs you" dot on More, with a paper ring so it
                        // reads clearly against the glyph. See the notification mock, option A.
                        if (tab.route == "more" && showMoreDot) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 5.dp, y = (-3).dp)
                                    .size(12.dp)
                                    .background(VitalColors.paper, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(VitalColors.orange, CircleShape)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        color = color,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        letterSpacing = 0.01.em,
                        // Tighten the glyph-to-label gap to match the mock: drop Android's
                        // extra font padding and pin the line box to the font size.
                        lineHeight = 10.sp,
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }
    }
}

/**
 * The divider above the tabs doubles as the rest-timer track: while a rest runs, an orange
 * fill drains from the left as time runs out. It carries no controls — skip and +15 s live in
 * the notification and on the Lift screen — so it stays a quiet, glanceable line.
 *
 * [progress] is remaining/total, or null when no rest is running.
 */
@Composable
private fun RestIndicator(progress: Float?) {
    val fill = animateFloatAsState(
        targetValue = (progress ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 260, easing = LinearEasing),
        label = "restFill"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(VitalColors.rule)
            // Redraw the drain instead of recomposing the nav every frame.
            .drawBehind {
                val value = fill.value
                if (value > 0f) {
                    drawRect(
                        color = VitalColors.orange,
                        size = Size(size.width * value, size.height)
                    )
                }
            }
    )
}

/** Custom stroke glyphs so the nav matches the editorial mocks, not Material icons. */
@Composable
fun VitalNavGlyph(
    tabIndex: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.7.dp.toPx()
        fun p(x: Float, y: Float) = Offset(x * w, y * h)
        val style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (tabIndex) {
            // Meals — fork and knife.
            0 -> {
                drawLine(color, p(0.28f, 0.12f), p(0.28f, 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, p(0.14f, 0.12f), p(0.14f, 0.40f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, p(0.42f, 0.12f), p(0.42f, 0.40f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, p(0.14f, 0.40f), p(0.42f, 0.40f), strokeWidth = stroke, cap = StrokeCap.Round)
                val blade = Path().apply {
                    moveTo(0.72f * w, 0.12f * h)
                    lineTo(0.86f * w, 0.30f * h)
                    lineTo(0.72f * w, 0.48f * h)
                }
                drawPath(blade, color, style = style)
                drawLine(color, p(0.72f, 0.48f), p(0.72f, 0.88f), strokeWidth = stroke, cap = StrokeCap.Round)
            }

            // Lift — dumbbell.
            1 -> {
                drawLine(color, p(0.22f, 0.50f), p(0.78f, 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, p(0.22f, 0.26f), p(0.22f, 0.74f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, p(0.78f, 0.26f), p(0.78f, 0.74f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, p(0.10f, 0.36f), p(0.10f, 0.64f), strokeWidth = stroke, cap = StrokeCap.Round)
                drawLine(color, p(0.90f, 0.36f), p(0.90f, 0.64f), strokeWidth = stroke, cap = StrokeCap.Round)
            }

            // Today — house.
            2 -> {
                val house = Path().apply {
                    moveTo(0.12f * w, 0.44f * h)
                    lineTo(0.50f * w, 0.12f * h)
                    lineTo(0.88f * w, 0.44f * h)
                    moveTo(0.22f * w, 0.40f * h)
                    lineTo(0.22f * w, 0.88f * h)
                    lineTo(0.78f * w, 0.88f * h)
                    lineTo(0.78f * w, 0.40f * h)
                }
                drawPath(house, color, style = style)
            }

            // Cardio — heartbeat trace.
            3 -> {
                val trace = Path().apply {
                    moveTo(0.06f * w, 0.55f * h)
                    lineTo(0.30f * w, 0.55f * h)
                    lineTo(0.42f * w, 0.24f * h)
                    lineTo(0.56f * w, 0.80f * h)
                    lineTo(0.66f * w, 0.55f * h)
                    lineTo(0.94f * w, 0.55f * h)
                }
                drawPath(trace, color, style = style)
            }

            // More — three dots.
            else -> {
                drawCircle(color, radius = 1.4.dp.toPx(), center = p(0.22f, 0.50f))
                drawCircle(color, radius = 1.4.dp.toPx(), center = p(0.50f, 0.50f))
                drawCircle(color, radius = 1.4.dp.toPx(), center = p(0.78f, 0.50f))
            }
        }
    }
}
