package com.vital.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.DesignUnitsProvider
import com.vital.ui.theme.duToDp
import com.vital.ui.theme.duToSp
import com.vital.ui.theme.VitalColors

@Composable
private fun duPx(du: Float): Float {
    val dp = du.duToDp()
    return with(LocalDensity.current) { dp.toPx() }
}

/** The QUICK ADD targets, in menu order. */
enum class QuickAddAction(val label: String) {
    NewWorkout("NEW WORKOUT"),
    LogMeal("LOG MEAL"),
    LogSet("LOG SET"),
    LogCardio("LOG CARDIO"),
    LogBodyWeight("LOG BODY WEIGHT")
}

/**
 * QUICK ADD bottom sheet (REMAINING_TABS_BRIEF section 2, FIXES brief defect 6).
 * Slides up from the bottom, sits directly above the glass nav, scrim over the page.
 */
@Composable
fun QuickAddSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onAction: (QuickAddAction) -> Unit
) {
    DesignUnitsProvider {
        BackHandler(enabled = isOpen) { onDismiss() }

        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim: black 50 %, fades in over 120 ms.
            AnimatedVisibility(
                visible = isOpen,
                enter = fadeIn(animationSpec = tween(120)),
                exit = fadeOut(animationSpec = tween(120))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(onClick = onDismiss)
                )
            }

            // Panel: bottom edge sits on the nav's top edge (14 + 60 = 74 du from the screen bottom).
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(220)
                ) + fadeIn(animationSpec = tween(120)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(160)
                ) + fadeOut(animationSpec = tween(160)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                QuickAddPanel(onAction = onAction)
            }
        }
    }
}

@Composable
private fun QuickAddPanel(onAction: (QuickAddAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 74.duToDp()) // clear the glass nav
            .clip(RoundedCornerShape(topStart = 26.duToDp(), topEnd = 26.duToDp()))
            .background(VitalColors.black)
    ) {
        // Sheet top highlight: 1 du at #1C1C1C.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.duToDp())
                .background(VitalColors.sheetTopHighlight)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.duToDp(), end = 16.duToDp(), top = 20.duToDp(), bottom = 20.duToDp())
        ) {
            Text(
                text = "QUICK ADD",
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.24.em,
                lineHeight = 13.duToSp(),
                modifier = Modifier.padding(bottom = 14.duToDp())
            )
            QuickAddAction.entries.forEachIndexed { index, action ->
                if (index > 0) Spacer(modifier = Modifier.height(8.duToDp()))
                QuickAddRow(
                    action = action,
                    onClick = { onAction(action) }
                )
            }
        }
    }
}

@Composable
private fun QuickAddRow(
    action: QuickAddAction,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.duToDp())
            .insetShadow(
                elevation = 2.duToDp(),
                cornerRadius = 12.duToDp(),
                shadowColor = VitalColors.black.copy(alpha = 0.4f)
            ),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(12.duToDp()),
        border = androidx.compose.foundation.BorderStroke(1.5f.duToDp(), VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.duToDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickAddGlyph(
                action = action,
                color = VitalColors.orange,
                modifier = Modifier.size(20.duToDp())
            )
            Spacer(modifier = Modifier.width(14.duToDp()))
            Text(
                text = action.label,
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.12.em,
                lineHeight = 13.duToSp(),
                textAlign = TextAlign.Start
            )
        }
    }
}

/** Stroke glyph, 20 du box, 1.8 du stroke, round caps and joins. */
@Composable
internal fun QuickAddGlyph(
    action: QuickAddAction,
    color: Color,
    modifier: Modifier = Modifier
) {
    val strokePx = duPx(1.8f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(x: Float, y: Float) = Offset(x * w, y * h)

        when (action) {
            QuickAddAction.NewWorkout -> {
                // Plan card with a plus: start a workout from the last one.
                drawRoundRect(
                    color = color,
                    topLeft = p(0.12f, 0.16f),
                    size = androidx.compose.ui.geometry.Size(0.76f * w, 0.68f * h),
                    cornerRadius = CornerRadius(0.10f * w, 0.10f * w),
                    style = stroke
                )
                drawLine(color, p(0.66f, 0.30f), p(0.66f, 0.50f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.56f, 0.40f), p(0.76f, 0.40f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.26f, 0.38f), p(0.46f, 0.38f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.26f, 0.60f), p(0.46f, 0.60f), strokeWidth = strokePx, cap = StrokeCap.Round)
            }

            QuickAddAction.LogMeal -> {
                // Fork on the left, knife on the right.
                drawLine(color, p(0.28f, 0.12f), p(0.28f, 0.88f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.14f, 0.12f), p(0.14f, 0.40f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.42f, 0.12f), p(0.42f, 0.40f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.14f, 0.40f), p(0.42f, 0.40f), strokeWidth = strokePx, cap = StrokeCap.Round)
                // Knife: straight blade tapering to the handle.
                val blade = Path().apply {
                    moveTo(0.72f * w, 0.12f * h)
                    lineTo(0.86f * w, 0.30f * h)
                    lineTo(0.72f * w, 0.48f * h)
                }
                drawPath(blade, color, style = stroke)
                drawLine(color, p(0.72f, 0.48f), p(0.72f, 0.88f), strokeWidth = strokePx, cap = StrokeCap.Round)
            }

            QuickAddAction.LogSet -> {
                // Dumbbell.
                drawLine(color, p(0.22f, 0.50f), p(0.78f, 0.50f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.22f, 0.26f), p(0.22f, 0.74f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.78f, 0.26f), p(0.78f, 0.74f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.10f, 0.36f), p(0.10f, 0.64f), strokeWidth = strokePx, cap = StrokeCap.Round)
                drawLine(color, p(0.90f, 0.36f), p(0.90f, 0.64f), strokeWidth = strokePx, cap = StrokeCap.Round)
            }

            QuickAddAction.LogCardio -> {
                // Heartbeat trace.
                val trace = Path().apply {
                    moveTo(0.06f * w, 0.55f * h)
                    lineTo(0.30f * w, 0.55f * h)
                    lineTo(0.42f * w, 0.24f * h)
                    lineTo(0.56f * w, 0.80f * h)
                    lineTo(0.66f * w, 0.55f * h)
                    lineTo(0.94f * w, 0.55f * h)
                }
                drawPath(trace, color, style = stroke)
            }

            QuickAddAction.LogBodyWeight -> {
                // Bathroom scale: rounded square with an arc gauge.
                drawRoundRect(
                    color = color,
                    topLeft = p(0.10f, 0.16f),
                    size = androidx.compose.ui.geometry.Size(0.80f * w, 0.68f * h),
                    cornerRadius = CornerRadius(0.16f * w, 0.16f * w),
                    style = stroke
                )
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = p(0.34f, 0.38f),
                    size = androidx.compose.ui.geometry.Size(0.32f * w, 0.32f * h),
                    style = stroke
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "QUICK ADD sheet", showBackground = true, backgroundColor = 0xFF000000, widthDp = 393, heightDp = 855)
@Composable
private fun QuickAddSheetPreview() {
    com.vital.ui.theme.VitalTheme(trueBlack = true) {
        Box(modifier = Modifier.fillMaxSize().background(VitalColors.black)) {
            QuickAddSheet(isOpen = true, onDismiss = {}, onAction = {})
        }
    }
}
