package com.vital.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.components.Sparkline
import com.vital.ui.components.Tag
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel

@Composable
fun ProgressScreen(
    onClose: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

    val currentWeight by viewModel.currentWeight.collectAsStateWithLifecycle(80f)
    val weightChange by viewModel.weightChange.collectAsStateWithLifecycle(0f)
    val bodyFatPct by viewModel.bodyFatPct.collectAsStateWithLifecycle(15f)
    val weeklyWeightData by viewModel.weeklyWeightData.collectAsStateWithLifecycle(emptyList())
    val weeklyLabels by viewModel.weeklyLabels.collectAsStateWithLifecycle(emptyList())
    val progressPhotos by viewModel.progressPhotos.collectAsStateWithLifecycle(emptyList())
    val measurements by viewModel.measurements.collectAsStateWithLifecycle(emptyList())

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = VitalColors.black
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top bar with close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tag(text = dayTag, backgroundColor = VitalColors.orange)
                Text(
                    text = "PROGRESS",
                    color = VitalColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onClose),
                    color = VitalColors.wellFill,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, VitalColors.wellStroke)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = VitalColors.textPrimary, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            // Stats cards row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressStatCard(
                    modifier = Modifier.weight(1f),
                    label = "WEIGHT",
                    value = String.format("%.1f kg", currentWeight),
                    delta = String.format("%+.1f", weightChange),
                    deltaColor = if (weightChange < 0) VitalColors.orange else VitalColors.orange,
                    icon = Icons.Default.MonitorWeight,
                    accentColor = VitalColors.orange
                )
                ProgressStatCard(
                    modifier = Modifier.weight(1f),
                    label = "BODY FAT",
                    value = String.format("%.1f%%", bodyFatPct),
                    delta = "--",
                    deltaColor = VitalColors.textMuted,
                    icon = Icons.Default.Straighten,
                    accentColor = VitalColors.orange
                )
                ProgressStatCard(
                    modifier = Modifier.weight(1f),
                    label = "PHOTOS",
                    value = progressPhotos.size.toString(),
                    delta = "TOTAL",
                    deltaColor = VitalColors.textMuted,
                    icon = Icons.Default.PhotoCamera,
                    accentColor = VitalColors.orange
                )
            }

            // Weight Trend Chart
            SectionHeader(
                title = "WEIGHT TREND",
                accentColor = VitalColors.orange,
                showMoreAction = { /* TODO: Show full weight history */ }
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(220.dp),
                color = VitalColors.wellFill,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalColors.wellStroke)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroLineGraph(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        dataPoints = weeklyWeightData,
                        labels = weeklyLabels,
                        accentColor = VitalColors.orange,
                        showTooltip = true,
                        tooltipIndex = weeklyWeightData.lastIndex,
                        tooltipValue = weeklyWeightData.lastOrNull() ?: 0f
                    )

                    // Sparklines
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sparkline(
                            modifier = Modifier.weight(1f).height(40.dp),
                            dataPoints = weeklyWeightData,
                            accentColor = VitalColors.orange
                        )
                        Sparkline(
                            modifier = Modifier.weight(1f).height(40.dp),
                            dataPoints = weeklyWeightData.map { it * 0.95f },
                            accentColor = VitalColors.orange.copy(alpha = 0.7f)
                        )
                        Sparkline(
                            modifier = Modifier.weight(1f).height(40.dp),
                            dataPoints = weeklyWeightData.map { it * 1.05f },
                            accentColor = VitalColors.orange.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Progress Photos
            SectionHeader(
                title = "PROGRESS PHOTOS",
                accentColor = VitalColors.orange
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
            ) {
                items(progressPhotos) { photo ->
                    ProgressPhotoCard(photo = photo)
                }

                if (progressPhotos.isEmpty()) {
                    item {
                        AddPhotoCard(onClick = { /* TODO: Open camera/gallery */ })
                    }
                } else {
                    item {
                        AddPhotoCard(onClick = { /* TODO: Open camera/gallery */ })
                    }
                }
            }

            // Measurements
            SectionHeader(
                title = "MEASUREMENTS",
                accentColor = VitalColors.orange
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 32.dp)
            ) {
                items(measurements) { measurement ->
                    MeasurementRow(
                        label = measurement.label,
                        value = measurement.value,
                        unit = measurement.unit,
                        delta = measurement.delta,
                        accentColor = VitalColors.orange
                    )
                }

                if (measurements.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO MEASUREMENTS RECORDED",
                                color = VitalColors.textMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    delta: String,
    deltaColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Surface(
        modifier = modifier
            .height(100.dp),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, VitalColors.wellStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = accentColor)
                Text(text = delta, color = deltaColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = VitalColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = VitalColors.textMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ProgressPhotoCard(photo: ProgressPhotoItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, VitalColors.wellStroke)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = VitalColors.orange.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Photo", tint = VitalColors.orange)
                        }
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = photo.type.uppercase(), color = VitalColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = photo.date, color = VitalColors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Text(text = photo.notes ?: "", color = VitalColors.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // Photo placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 12.dp),
                color = VitalColors.wellFill,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, VitalColors.wellStroke)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Progress photo", tint = VitalColors.textMuted.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Text(text = "PHOTO: ${photo.type}", color = VitalColors.textMuted.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPhotoCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, VitalColors.orange.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Add photo", tint = VitalColors.orange, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ADD PROGRESS PHOTO", color = VitalColors.orange, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun MeasurementRow(
    label: String,
    value: String,
    unit: String,
    delta: String,
    accentColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = VitalColors.wellFill,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, VitalColors.wellStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.size(40.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = label.substring(0, 1), color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = label.uppercase(), color = VitalColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "$value $unit", color = VitalColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (delta.isNotBlank()) {
                    Text(text = delta, color = VitalColors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

data class ProgressPhotoItem(
    val type: String, // front, side, back
    val date: String,
    val imagePath: String,
    val notes: String? = null
)

data class MeasurementItem(
    val label: String,
    val value: String,
    val unit: String,
    val delta: String
)

object ProgressScreenPreviewData {
    val samplePhotos = listOf(
        ProgressPhotoItem("front", "JAN 15", "/path/to/photo1.jpg", "Starting point"),
        ProgressPhotoItem("side", "JAN 15", "/path/to/photo2.jpg", ""),
        ProgressPhotoItem("back", "JAN 15", "/path/to/photo3.jpg", "")
    )
    val sampleMeasurements = listOf(
        MeasurementItem("Waist", "82", "cm", "-2"),
        MeasurementItem("Chest", "105", "cm", "+1"),
        MeasurementItem("Arms", "36", "cm", "+0.5"),
        MeasurementItem("Thighs", "58", "cm", "+1")
    )
}