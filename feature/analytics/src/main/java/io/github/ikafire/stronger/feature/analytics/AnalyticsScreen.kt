package io.github.ikafire.stronger.feature.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ikafire.stronger.core.domain.model.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Analytics") })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            // Time period selector
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    TimePeriod.entries.forEach { period ->
                        FilterChip(
                            selected = uiState.selectedPeriod == period,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(period.label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }

            // Muscle Heat Map
            item {
                Text(
                    text = "Muscle Volume Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            if (uiState.muscleVolumes.isEmpty() && !uiState.isLoading) {
                item {
                    Text(
                        text = "No workout data for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }

            // Heat map as a bar chart
            item {
                if (uiState.muscleVolumes.isNotEmpty()) {
                    MuscleHeatMap(
                        data = uiState.muscleVolumes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((uiState.muscleVolumes.size * 40 + 16).dp),
                    )
                }
            }

            // Muscle detail cards
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Muscle Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            items(uiState.muscleVolumes) { data ->
                MuscleVolumeCard(data = data, maxVolume = uiState.muscleVolumes.firstOrNull()?.totalVolume ?: 1.0)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MuscleHeatMap(
    data: List<MuscleVolumeData>,
    modifier: Modifier = Modifier,
) {
    val maxVolume = data.maxOfOrNull { it.totalVolume } ?: 1.0
    val barColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceContainerLow

    Canvas(modifier = modifier) {
        val barHeight = 28f
        val spacing = 12f

        data.forEachIndexed { index, item ->
            val y = index * (barHeight + spacing)
            val barWidth = ((item.totalVolume / maxVolume) * size.width * 0.7f).toFloat()

            // Background bar
            drawRect(
                color = bgColor,
                topLeft = Offset(size.width * 0.3f, y),
                size = Size(size.width * 0.7f, barHeight),
            )

            // Value bar
            drawRect(
                color = barColor.copy(alpha = 0.3f + 0.7f * (item.totalVolume / maxVolume).toFloat()),
                topLeft = Offset(size.width * 0.3f, y),
                size = Size(barWidth, barHeight),
            )
        }
    }

    // Overlay muscle labels
    Column(modifier = modifier) {
        data.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = item.muscle.displayName(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(100.dp),
                )
            }
        }
    }
}

@Composable
private fun MuscleVolumeCard(
    data: MuscleVolumeData,
    maxVolume: Double,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = data.muscle.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${data.setCount} sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (data.totalVolume / maxVolume).toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${"%.0f".format(data.totalVolume)} kg total volume",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun MuscleGroup.displayName(): String = when (this) {
    MuscleGroup.CHEST -> "Chest"
    MuscleGroup.BACK -> "Back"
    MuscleGroup.SHOULDERS -> "Shoulders"
    MuscleGroup.BICEPS -> "Biceps"
    MuscleGroup.TRICEPS -> "Triceps"
    MuscleGroup.FOREARMS -> "Forearms"
    MuscleGroup.CORE -> "Core"
    MuscleGroup.QUADS -> "Quads"
    MuscleGroup.HAMSTRINGS -> "Hamstrings"
    MuscleGroup.GLUTES -> "Glutes"
    MuscleGroup.CALVES -> "Calves"
    MuscleGroup.TRAPS -> "Traps"
    MuscleGroup.LATS -> "Lats"
    MuscleGroup.NECK -> "Neck"
    MuscleGroup.ADDUCTORS -> "Adductors"
    MuscleGroup.ABDUCTORS -> "Abductors"
    MuscleGroup.FULL_BODY -> "Full Body"
    MuscleGroup.CARDIO -> "Cardio"
    MuscleGroup.OTHER -> "Other"
}
