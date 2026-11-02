package com.vital.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vital.ui.theme.duToSp
import com.vital.ui.theme.VitalColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

@Composable
fun GlassBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onPlusClick: () -> Unit
) {
    val tabLabels = listOf("TDY", "MLS", "LFT", "CRD", "WKS", "SET")
    val tabCount = tabLabels.size

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 14.dp)
            .padding(horizontal = 12.dp),
        color = VitalColors.navFill,
        shape = RoundedCornerShape(30.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        shadowElevation = 24.dp
    ) {
        // Glass highlights - inner top edge white 38%
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.38f),
                            Color.White.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = 60f
                    )
                )
                .clip(RoundedCornerShape(30.dp))
        ) {}

        // Content row with tabs and plus button
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First three tabs: TDY, MLS, LFT
            for (i in 0..2) {
                val isSelected = selectedTab == i
                GlassTabItem(
                    label = tabLabels[i],
                    isSelected = isSelected,
                    onClick = { onTabSelected(i) }
                )
            }

            // Plus button in center
            GlassPlusButton(onClick = onPlusClick)

            // Last three tabs: CRD, WKS, SET (indices 3, 4, 5)
            for (i in 3..5) {
                val isSelected = selectedTab == i
                GlassTabItem(
                    label = tabLabels[i],
                    isSelected = isSelected,
                    onClick = { onTabSelected(i) }
                )
            }
        }
    }
}

@Composable
private fun GlassTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(48.dp)
            .height(46.dp)
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) VitalColors.orange else Color.Transparent,
        shape = RoundedCornerShape(23.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                // Inner top highlight white 40%
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .background(Color.White.copy(alpha = 0.4f))
                )
            }
            Text(
                text = label,
                color = if (isSelected) VitalColors.black else VitalColors.tabInactive,
                fontSize = 10.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.12.em
            )
        }
    }
}

@Composable
private fun GlassPlusButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .padding(horizontal = 3.dp)
            .clickable(onClick = onClick),
        color = VitalColors.black.copy(alpha = 0.55f), // black 55%
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, VitalColors.orange.copy(alpha = 0.75f)) // orange 75%
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp) // account for border
                .background(VitalColors.black.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick add",
                tint = VitalColors.orange,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickAddSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onLogMeal: () -> Unit,
    onLogSet: () -> Unit,
    onLogCardio: () -> Unit,
    onLogBodyWeight: () -> Unit
) {
    if (!isOpen) return

    // Animated slide up from bottom - use mutable state with animation
    val offsetPx = remember { mutableStateOf(300f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetPx.value,
        animationSpec = tween(300, delayMillis = 0, easing = { fraction -> 1f - (1f - fraction) * (1f - fraction) * (1f - fraction) }) // ease out cubic
    )

    // Trigger animation when sheet opens
    androidx.compose.runtime.LaunchedEffect(Unit) {
        offsetPx.value = 0f
    }

    // Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {}

    // Sheet content - slides up from bottom
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = animatedOffset.dp)
            .background(VitalColors.wellFill, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(bottom = 16.dp) // account for nav bar
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 24.dp), // extra padding for gesture area
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "QUICK ADD",
                color = VitalColors.textPrimary,
                fontSize = 13.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.24.em,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            QuickAddRow(
                label = "LOG MEAL",
                icon = Icons.Default.Restaurant,
                onClick = {
                    onLogMeal()
                    onDismiss()
                }
            )
            QuickAddRow(
                label = "LOG SET",
                icon = Icons.Default.FitnessCenter,
                onClick = {
                    onLogSet()
                    onDismiss()
                }
            )
            QuickAddRow(
                label = "LOG CARDIO",
                icon = Icons.Default.DirectionsRun,
                onClick = {
                    onLogCardio()
                    onDismiss()
                }
            )
            QuickAddRow(
                label = "LOG BODY WEIGHT",
                icon = Icons.Default.MonitorWeight,
                onClick = {
                    onLogBodyWeight()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun QuickAddRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = VitalColors.orange,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = VitalColors.textPrimary,
                fontSize = 16.duToSp(),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}