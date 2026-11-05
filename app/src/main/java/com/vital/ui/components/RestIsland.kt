package com.vital.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.R
import com.vital.ui.LiveIslandState
import com.vital.ui.formatRestTime
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import kotlinx.coroutines.delay

/** How long the expanded card stays up before folding back to the compact pill. */
private const val AUTO_COLLAPSE_MS = 8_000L
private const val WARN_SECONDS = 30

/**
 * The live island for the running rest timer: a compact pill beside the camera cutout that
 * expands into an action card. Only one activity is shown (option A -> B); it floats over
 * the page and never blocks the bottom nav.
 */
@Composable
fun RestIsland(
    state: LiveIslandState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onExtend: () -> Unit,
    onSkip: () -> Unit,
    onOpenLift: () -> Unit,
    onHeightChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Fold the card shortly after it opens so it never becomes permanent furniture.
    LaunchedEffect(expanded) {
        if (expanded) {
            delay(AUTO_COLLAPSE_MS)
            onExpandedChange(false)
        }
    }

    // Pop open once when the rest runs low, the one moment it is worth interrupting.
    LaunchedEffect(state.restRemaining) {
        if (state.restRemaining == WARN_SECONDS) onExpandedChange(true)
    }

    // A finished rest removes the island; drop the expanded flag with it.
    LaunchedEffect(state.isActive) {
        if (!state.isActive) onExpandedChange(false)
    }

    if (!state.isActive) return

    val surface = VitalColors.islandFill
    val shape = RoundedCornerShape(if (expanded) 26.dp else 999.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(shape)
            .background(surface)
            .then(
                if (expanded) {
                    Modifier.width(330.dp).padding(14.dp)
                } else {
                    Modifier
                        .height(38.dp)
                        .padding(horizontal = 12.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onExpandedChange(true) }
                }
            )
            .animateContentSize()
            .onGloballyPositioned { coords -> onHeightChanged(coords.size.height) }
    ) {
        if (expanded) {
            ExpandedIsland(
                state = state,
                onExtend = onExtend,
                onSkip = onSkip,
                onOpenLift = {
                    onExpandedChange(false)
                    onOpenLift()
                }
            )
        } else {
            CompactIsland(state = state)
        }
    }
}

// ---- compact -------------------------------------------------------------------------------

@Composable
private fun CompactIsland(state: LiveIslandState) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        IslandRing(progress = state.progress, size = 20.dp, stroke = 2.5.dp)
        Text(
            text = "Rest",
            color = VitalColors.islandInk,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = formatRestTime(state.restRemaining),
            color = VitalColors.islandInkMuted,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp
        )
        Spacer(Modifier.width(1.dp))
        LiveMark()
    }
}

@Composable
private fun LiveMark() {
    val transition = rememberInfiniteTransition(label = "live")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveAlpha"
    )
    Image(
        painter = painterResource(R.drawable.ic_vital_mark),
        contentDescription = null,
        modifier = Modifier
            .size(18.dp)
            .alpha(alpha)
    )
}

// ---- expanded ------------------------------------------------------------------------------

@Composable
private fun ExpandedIsland(
    state: LiveIslandState,
    onExtend: () -> Unit,
    onSkip: () -> Unit,
    onOpenLift: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IslandRing(progress = state.progress, size = 34.dp, stroke = 3.5.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rest running",
                    color = VitalColors.islandInk,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.01).em
                )
                Text(
                    text = "${formatRestTime(state.restRemaining)} left · " +
                        "target ${formatRestTime(state.restTotal)}",
                    color = VitalColors.islandInkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            LiveMark()
        }

        ProgressHairline(
            progress = state.progress,
            modifier = Modifier.padding(top = 11.dp, bottom = 10.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IslandAction(
                text = "+15 sec",
                filled = false,
                onClick = onExtend,
                modifier = Modifier.weight(1f)
            )
            IslandAction(
                text = "Done",
                filled = true,
                onClick = onSkip,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LIFT · ${(state.exerciseName ?: "—").uppercase()}",
                color = VitalColors.islandInkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 9.sp,
                letterSpacing = 0.06.em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onOpenLift() }
            )
            Text(
                text = "OPEN",
                color = VitalColors.orange,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.06.em,
                modifier = Modifier.clickable { onOpenLift() }
            )
        }
    }
}

@Composable
private fun ProgressHairline(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(VitalColors.islandRule, RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(VitalColors.orange, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun IslandAction(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (filled) VitalColors.orange else VitalColors.islandAction,
        label = "action"
    )
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (filled) VitalColors.onAccent else VitalColors.islandInk,
            fontFamily = VitalFonts.InterTight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun IslandRing(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    stroke: androidx.compose.ui.unit.Dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val width = stroke.toPx()
        val radius = this.size.minDimension / 2f - width / 2f
        val topLeft = Offset(
            this.size.width / 2f - radius,
            this.size.height / 2f - radius
        )
        val arcSize = Size(radius * 2f, radius * 2f)
        drawCircle(
            color = VitalColors.islandRule,
            radius = radius,
            style = Stroke(width = width)
        )
        if (progress > 0f) {
            drawArc(
                color = VitalColors.orange,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = width, cap = StrokeCap.Round)
            )
        }
    }
}
