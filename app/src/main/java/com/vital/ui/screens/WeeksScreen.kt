package com.vital.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.Sparkline
import com.vital.ui.components.SummaryDial
import com.vital.ui.components.Tag
import com.vital.ui.components.QuickStat
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel

@Composable
fun WeeksScreen(
    modifier: Modifier = Modifier,
    onShowSummary: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToLift: () -> Unit,
    onNavigateToCardio: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

    val period by viewModel.weeksPeriod.collectAsStateWithLifecycle(WeeksPeriod.L)
    val weeklyMacroData by viewModel.weeklyMacroData.collectAsStateWithLifecycle(emptyList())
    val weeklyVolumeData by viewModel.weeklyVolumeData.collectAsStateWithLifecycle(emptyList())
    val weeklyWeightData by viewModel.weeklyWeightData.collectAsStateWithLifecycle(emptyList())
    val weeklyPrData by viewModel.weeklyPrData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels12.collectAsStateWithLifecycle(emptyList())

    Surface(
        modifier = modifier.fillMaxSize(),
        color = VitalColors.Background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Top Bar with period switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tag(text = dayTag, backgroundColor = VitalColors.AccentWKS)
                Text(
                    text = "WEEKS",
                    color = VitalColors.PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Tag(text = dateTag, backgroundColor = VitalColors.AccentWKS)
            }

            // Period Switch (L / M / H = 12w / 4w / 7d)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PeriodButton(modifier = Modifier.weight(1f), label = "L", labelFull = "12 WEEKS", isSelected = period == WeeksPeriod.L, accentColor = VitalColors.AccentWKS) { /* viewModel.setWeeksPeriod(WeeksPeriod.L) */ }
                PeriodButton(modifier = Modifier.weight(1f), label = "M", labelFull = "4 WEEKS", isSelected = period == WeeksPeriod.M, accentColor = VitalColors.AccentWKS) { /* viewModel.setWeeksPeriod(WeeksPeriod.M) */ }
                PeriodButton(modifier = Modifier.weight(1f), label = "H", labelFull = "7 DAYS", isSelected = period == WeeksPeriod.H, accentColor = VitalColors.AccentWKS) { /* viewModel.setWeeksPeriod(WeeksPeriod.H) */ }
            }

            // Hero Card on Accent Fill - Macro Adherence
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(280.dp),
                color = VitalColors.AccentWKS,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Tag(
                            text = "MACRO ADHERENCE",
                            backgroundColor = Color.Black.copy(alpha = 0.3f),
                            textColor = Color.Black
                        )
                        Text(text = period.label, color = Color.Black.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    // Adherence percentage
                    BigKcalDisplay(
                        remaining = (weeklyMacroData.lastOrNull() ?: 0f) * 100,
                        unit = "%",
                        accentColor = Color.Black
                    )

                    // Macro breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickStat(label = "P", value = "95%", color = Color.Black)
                        QuickStat(label = "C", value = "92%", color = Color.Black)
                        QuickStat(label = "F", value = "98%", color = Color.Black)
                    }

                    // Hero Line Graph (inset grey panel)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(top = 8.dp),
                        color = VitalColors.InsetGreyPanel,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        HeroLineGraph(
                            modifier = Modifier.fillMaxSize(),
                            dataPoints = weeklyMacroData,
                            labels = weeklyLabels,
                            accentColor = Color.Black,
                            showTooltip = true,
                            tooltipIndex = weeklyMacroData.lastIndex,
                            tooltipValue = (weeklyMacroData.lastOrNull() ?: 0f) * 100
                        )
                    }
                }
            }

            // VOLUME Chart
            SectionHeader(
                title = "VOLUME",
                accentColor = VitalColors.AccentWKS,
                showMoreAction = onShowSummary
            )

            ChartCard(
                title = "WEEKLY VOLUME (KG)",
                dataPoints = weeklyVolumeData,
                labels = weeklyLabels,
                accentColor = VitalColors.AccentLFT,
                sparklineData = weeklyVolumeData
            )

            // BODY WEIGHT Chart
            SectionHeader(
                title = "BODY WEIGHT",
                accentColor = VitalColors.AccentWKS
            )

            ChartCard(
                title = "WEIGHT TREND (KG)",
                dataPoints = weeklyWeightData,
                labels = weeklyLabels,
                accentColor = VitalColors.AccentCRD,
                sparklineData = weeklyWeightData
            )

            // PRs Chart
            SectionHeader(
                title = "PERSONAL RECORDS",
                accentColor = VitalColors.AccentWKS
            )

            ChartCard(
                title = "PR PROGRESSION",
                dataPoints = weeklyPrData,
                labels = weeklyLabels,
                accentColor = VitalColors.AccentTDY,
                sparklineData = weeklyPrData
            )
        }
    }
}

enum class WeeksPeriod(val label: String) {
    L("12W"),
    M("4W"),
    H("7D")
}

@Composable
fun PeriodButton(
    modifier: Modifier = Modifier,
    label: String,
    labelFull: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) accentColor else VitalColors.NearBlackRow,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, if (isSelected) Color.Transparent else VitalColors.NearBlackRowStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, color = if (isSelected) Color.Black else accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = labelFull, color = if (isSelected) Color.Black.copy(alpha = 0.7f) else VitalColors.MutedText, fontSize = 8.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    dataPoints: List<Float>,
    labels: List<String>,
    accentColor: Color,
    sparklineData: List<Float>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(180.dp),
        color = VitalColors.Card,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, VitalColors.NearBlackRowStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(text = title, color = VitalColors.MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

            // Hero Line Graph
            HeroLineGraph(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                dataPoints = dataPoints,
                labels = labels,
                accentColor = accentColor,
                showTooltip = true,
                tooltipIndex = dataPoints.lastIndex,
                tooltipValue = dataPoints.lastOrNull() ?: 0f
            )

            // Sparklines for sub-metrics
            if (sparklineData.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Sparkline(
                        modifier = Modifier.weight(1f).height(40.dp),
                        dataPoints = sparklineData,
                        accentColor = accentColor
                    )
                    Sparkline(
                        modifier = Modifier.weight(1f).height(40.dp),
                        dataPoints = sparklineData.map { it * 0.8f },
                        accentColor = accentColor.copy(alpha = 0.7f)
                    )
                    Sparkline(
                        modifier = Modifier.weight(1f).height(40.dp),
                        dataPoints = sparklineData.map { it * 1.2f },
                        accentColor = accentColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

object WeeksScreenPreviewData {
    val weeklyMacro = listOf(0.85f, 0.92f, 0.88f, 0.95f, 0.91f, 0.94f, 0.93f, 0.89f, 0.96f, 0.90f, 0.94f, 0.92f)
    val weeklyVolume = listOf(0.2f, 0.35f, 0.45f, 0.38f, 0.52f, 0.61f, 0.55f, 0.48f, 0.58f, 0.50f, 0.62f, 0.57f)
    val weeklyWeight = listOf(0.5f, 0.48f, 0.49f, 0.47f, 0.46f, 0.45f, 0.44f, 0.43f, 0.42f, 0.41f, 0.40f, 0.39f)
    val weeklyPrs = listOf(0.3f, 0.35f, 0.4f, 0.42f, 0.45f, 0.48f, 0.5f, 0.52f, 0.55f, 0.58f, 0.6f, 0.62f)
    val weeklyLabels12 = listOf("W-11", "W-10", "W-9", "W-8", "W-7", "W-6", "W-5", "W-4", "W-3", "W-2", "W-1", "THIS")
}
