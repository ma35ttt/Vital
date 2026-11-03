package com.vital.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.components.FabGlyph
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.KnurledKnob
import com.vital.ui.components.OrangeDial
import com.vital.ui.components.Period
import com.vital.ui.components.ScreenScaffold
import com.vital.ui.components.ScreenVariant
import com.vital.ui.components.Sparkline
import com.vital.ui.components.SummaryDial
import com.vital.ui.components.Tag
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.insetShadow
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.duToDp

// Debug-only screen (SPEC.md §9 Step 2, ROADMAP M2-F4): renders each canvas component
// in isolation so device screenshots can be compared against the measured numbers.
// Reachable only from Settings > ABOUT > DEBUG SHOWCASE in a debuggable build.
@Composable
fun DebugShowcaseScreen(modifier: Modifier = Modifier) {
    var knobProgress by remember { mutableStateOf(0.5f) }
    var period by remember { mutableStateOf(Period.M) }

    Surface(modifier = modifier.fillMaxSize(), color = VitalColors.black) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "M2 DESIGN SYSTEM SHOWCASE",
                    color = VitalColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                SectionLabel("1 SUMMARY DIAL (ref frame-01 right)")
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(560.dp),
                    color = VitalColors.orange,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    SummaryDial(
                        modifier = Modifier.fillMaxSize(),
                        kcalRemaining = 306f,
                        kcalTarget = 2500f,
                        proteinPercent = 0.33f,
                        carbsPercent = 0.4f,
                        fatPercent = 0.27f,
                        period = period,
                        onPeriodChange = { period = it },
                        accentColor = VitalColors.orange,
                        showKnob = true,
                        onKnobProgressChange = {}
                    )
                }
            }

            item {
                SectionLabel("2 KNURLED KNOB 200dp (drag test)")
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    KnurledKnob(
                        modifier = Modifier.size(200.dp),
                        progress = knobProgress,
                        onProgressChange = { knobProgress = it },
                        accentColor = VitalColors.DialKnobHand
                    )
                    Text(
                        text = "PROGRESS ${(knobProgress * 100).toInt()}%",
                        color = VitalColors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            item {
                SectionLabel("3 HERO CARD + GRAPH (ref frame-01 left)")
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(380.dp),
                    color = VitalColors.orange,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Tag(text = "MOVE", backgroundColor = Color.Black.copy(alpha = 0.3f), textColor = Color.White)
                            Text("NOW", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("09:24", color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Stat("MOVE", "500CAL")
                            Stat("DISTANCE", "1.821KM")
                            Stat("PULSE", "135BPM")
                        }
                        HeroLineGraph(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp),
                            dataPoints = listOf(0.42f, 0.45f, 0.40f, 0.52f, 0.38f, 0.55f, 0.48f, 0.55f),
                            labels = listOf("6m", "7m", "8m", "9m", "10m", "11m", "12m", "13m"),
                            accentColor = Color.White,
                            showTooltip = true,
                            tooltipIndex = 5,
                            tooltipValue = 111f
                        )
                    }
                }
            }

            item {
                SectionLabel("4 CAPSULE + SPARKLINE (ref frame-01 capsules)")
                capsuleRow(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    icon = { Icon(Icons.Default.DirectionsRun, "", tint = VitalColors.orange) },
                    label = "HIKING",
                    value = ".980KM",
                    sparklineData = listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f, 0.62f),
                    accentColor = VitalColors.orange
                )
            }

            item {
                SectionLabel("5 SPARKLINE IN GREY PANEL")
                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(90.dp),
                    color = VitalColors.panelGrey,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Text("TODAY", color = VitalColors.black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Sparkline(
                            modifier = Modifier.fillMaxWidth().height(40.dp).padding(top = 2.dp),
                            dataPoints = listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.55f, 0.62f),
                            accentColor = VitalColors.black
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("960", color = VitalColors.black, fontSize = 8.sp)
                            Text("980", color = VitalColors.black, fontSize = 8.sp)
                            Text("990", color = VitalColors.black, fontSize = 8.sp)
                        }
                    }
                }
            }

            item {
                SectionLabel("6 INSET SHADOW")
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(110.dp)
                        .insetShadow(elevation = 10.dp, cornerRadius = 16.dp),
                    color = VitalColors.wellFill,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "INSET SHADOW",
                            color = VitalColors.textMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            item {
                SectionLabel("7 SCREEN SCAFFOLD - ORANGE HERO (MEALS)")
                Box(modifier = Modifier.fillMaxWidth().height(640.dp)) {
                    ScreenScaffold(
                        variant = ScreenVariant.OrangeHero,
                        title = "MEALS",
                        heroLabel = "KCAL LEFT",
                        heroValue = "1,240",
                        onShowMore = {},
                        dialLetters = listOf("P", "C", "F"),
                        dialHighlightedIndex = 0,
                        dialInnerLabel = "EATEN",
                        dialInnerValue = "1,260",
                        dialInnerUnit = "",
                        dialPillText = "OF 2,500",
                        dialTopPill = "LUNCH",
                        dialFabGlyph = FabGlyph.Plus
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Sheet content area", color = VitalColors.textPrimary)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 32.dp))
                            Text(text = "Bottom of sheet", color = VitalColors.textMuted)
                        }
                    }
                }
            }

            item {
                SectionLabel("8 SCREEN SCAFFOLD - BLACK PAGE (WEEKS)")
                Box(modifier = Modifier.fillMaxWidth().height(640.dp)) {
                    ScreenScaffold(
                        variant = ScreenVariant.BlackPage,
                        title = "WEEKS"
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Black page content", color = VitalColors.textPrimary)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(vertical = 32.dp))
                            Text(text = "Scrolls with black backing", color = VitalColors.textMuted)
                        }
                    }
                }
            }

            item {
                SectionLabel("9 ORANGE DIAL - MEALS (P / C / F)")
                DialStage {
                    OrangeDial(
                        modifier = Modifier.fillMaxSize(),
                        letters = listOf("P", "C", "F"),
                        highlightedIndex = 0,
                        innerLabel = "EATEN",
                        innerValue = "1,260",
                        innerUnit = "",
                        pillText = "OF 2,500",
                        topPill = "LUNCH",
                        fabGlyph = FabGlyph.Plus
                    )
                }
            }

            item {
                SectionLabel("10 ORANGE DIAL - LIFT (2 / 3 / 4)")
                DialStage {
                    OrangeDial(
                        modifier = Modifier.fillMaxSize(),
                        letters = listOf("2", "3", "4"),
                        highlightedIndex = 1,
                        innerLabel = "BENCH PRESS",
                        innerValue = "80",
                        innerUnit = "kg",
                        pillText = "SET 3 · ×8",
                        topPill = "OF 3:00",
                        fabGlyph = FabGlyph.Plus
                    )
                }
            }

            item {
                SectionLabel("11 ORANGE DIAL - CARDIO (R / B / W)")
                DialStage {
                    OrangeDial(
                        modifier = Modifier.fillMaxSize(),
                        letters = listOf("R", "B", "W"),
                        highlightedIndex = 0,
                        innerLabel = "SPEED",
                        innerValue = "6.0",
                        innerUnit = "km/h",
                        pillText = "INCL 2.0%",
                        topPill = "32:10",
                        fabGlyph = FabGlyph.Camera
                    )
                }
            }
        }
    }
}

// Orange hero stage: 430 du tall so the full FAB (bottom 414) is visible.
@Composable
private fun DialStage(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.duToDp())
            .background(VitalColors.orange)
    ) {
        content()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = VitalColors.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
