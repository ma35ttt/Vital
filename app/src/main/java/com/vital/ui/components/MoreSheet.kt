package com.vital.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The "More" sheet that replaces the old Weeks / Settings / Summary tabs.
 * Quick add stays at the top; the rest of the app's secondary destinations sit below it.
 */
@Composable
fun MoreSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onQuickAdd: (QuickAddAction) -> Unit,
    onNavigate: (String) -> Unit,
    showDebug: Boolean = false
) {
    BackHandler(enabled = isOpen) { onDismiss() }

    val scope = rememberCoroutineScope()
    var dragOffset by remember { mutableStateOf(0f) }
    val dismissThreshold = with(LocalDensity.current) { 96.dp.toPx() }

    // Start each open from rest, not from wherever the last drag left the panel.
    LaunchedEffect(isOpen) {
        if (isOpen) dragOffset = 0f
    }
    val dragState = rememberDraggableState { delta ->
        dragOffset = (dragOffset + delta).coerceAtLeast(0f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .clickable(onClick = onDismiss)
            )
        }

        AnimatedVisibility(
            visible = isOpen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f)
            ) + fadeIn(animationSpec = tween(150)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Follow the finger, then settle back or dismiss.
                    .offset { IntOffset(0, dragOffset.roundToInt()) }
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                        onDragStopped = { velocity ->
                            if (dragOffset > dismissThreshold || velocity > 1_100f) {
                                onDismiss()
                            } else {
                                scope.launch {
                                    animate(
                                        initialValue = dragOffset,
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f)
                                    ) { value, _ -> dragOffset = value }
                                }
                            }
                        }
                    )
                    .padding(bottom = (NAV_ROW_HEIGHT_DP + 1).dp)
                    .navigationBarsPadding()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(VitalColors.sheet)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(VitalColors.ruleStrong)
                )
                MoreSheetContent(
                    onDismiss = onDismiss,
                    onQuickAdd = onQuickAdd,
                    onNavigate = onNavigate,
                    showDebug = showDebug
                )
            }
        }
    }
}

@Composable
private fun MoreSheetContent(
    onDismiss: () -> Unit,
    onQuickAdd: (QuickAddAction) -> Unit,
    onNavigate: (String) -> Unit,
    showDebug: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 18.dp, bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(2.dp)
                .background(VitalColors.ruleStrong)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "More",
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.025).em
            )
            Text(
                text = "Done",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }

        EditorialEyebrow(
            text = "Quick add",
            color = VitalColors.inkMuted,
            modifier = Modifier.padding(top = 26.dp, bottom = 2.dp)
        )
        QuickAddAction.entries.forEach { action ->
            MoreRow(
                label = action.label.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { onQuickAdd(action) },
                glyph = {
                    QuickAddGlyph(
                        action = action,
                        color = VitalColors.orange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        EditorialEyebrow(
            text = "Go to",
            color = VitalColors.inkMuted,
            modifier = Modifier.padding(top = 26.dp, bottom = 2.dp)
        )
        MoreRow(label = "Summary", onClick = { onNavigate("summary") }, trailing = "›")
        MoreRow(label = "Weeks", onClick = { onNavigate("weeks") }, trailing = "›")
        MoreRow(label = "Exercises", onClick = { onNavigate("exercises") }, trailing = "›")
        MoreRow(label = "Settings", onClick = { onNavigate("settings") }, trailing = "›")
        MoreRow(label = "AI coach", onClick = { onNavigate("coach") }, trailing = "›")
        if (showDebug) {
            MoreRow(label = "Debug showcase", onClick = { onNavigate("debug") }, trailing = "›")
        }
    }
}

@Composable
private fun MoreRow(
    label: String,
    onClick: () -> Unit,
    trailing: String? = null,
    glyph: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (glyph != null) {
                glyph()
                Spacer(modifier = Modifier.width(14.dp))
            }
            Text(
                text = label,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 15.sp
                )
            }
        }
        Hairline()
    }
}
