package io.github.ikafire.reforge.feature.measure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ikafire.reforge.core.domain.model.BodyMeasurement
import io.github.ikafire.reforge.core.domain.model.MeasurementType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureScreen(
    viewModel: MeasureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var logType by remember { mutableStateOf<MeasurementType?>(null) }

    if (uiState.selectedType != null) {
        MeasurementDetailScreen(
            type = uiState.selectedType!!,
            history = uiState.selectedHistory,
            onBack = { viewModel.selectType(null) },
            onLog = { logType = uiState.selectedType },
            onDelete = viewModel::deleteMeasurement,
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Measure") })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                Text(
                    text = "Vitals",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            val vitals = listOf(MeasurementType.WEIGHT, MeasurementType.BODY_FAT, MeasurementType.CALORIC_INTAKE)
            items(vitals) { type ->
                MeasurementRow(
                    type = type,
                    latest = uiState.latestByType[type],
                    onClick = { viewModel.selectType(type) },
                    onLog = { logType = type },
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Body Parts",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            val bodyParts = MeasurementType.entries.filter { it !in vitals }
            items(bodyParts) { type ->
                MeasurementRow(
                    type = type,
                    latest = uiState.latestByType[type],
                    onClick = { viewModel.selectType(type) },
                    onLog = { logType = type },
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    logType?.let { type ->
        LogMeasurementDialog(
            type = type,
            onConfirm = { value ->
                viewModel.logMeasurement(type, value)
                logType = null
            },
            onDismiss = { logType = null },
        )
    }
}

@Composable
private fun MeasurementRow(
    type: MeasurementType,
    latest: BodyMeasurement?,
    onClick: () -> Unit,
    onLog: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(type.displayName(), style = MaterialTheme.typography.bodyLarge)
            if (latest != null) {
                Text(
                    text = "${latest.value} ${latest.unit.name.lowercase()} - ${latest.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "No data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onLog) {
            Icon(Icons.Default.Add, contentDescription = "Log")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementDetailScreen(
    type: MeasurementType,
    history: List<BodyMeasurement>,
    onBack: () -> Unit,
    onLog: () -> Unit,
    onDelete: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(type.displayName()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onLog) {
                        Icon(Icons.Default.Add, contentDescription = "Log")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            if (history.size >= 2) {
                MeasurementChart(
                    data = history.takeLast(30).map { it.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn {
                items(history.reversed(), key = { it.id }) { measurement ->
                    var showDelete by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { showDelete = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${measurement.value} ${measurement.unit.name.lowercase()}")
                            Text(
                                text = measurement.date.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (showDelete) {
                        AlertDialog(
                            onDismissRequest = { showDelete = false },
                            title = { Text("Delete Entry?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    onDelete(measurement.id)
                                    showDelete = false
                                }) { Text("Delete") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        val minVal = data.min()
        val maxVal = data.max()
        val range = (maxVal - minVal).coerceAtLeast(1.0)

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = (index.toFloat() / (data.size - 1)) * size.width
            val y = size.height - ((value - minVal) / range * size.height * 0.9f).toFloat() - size.height * 0.05f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, lineColor, style = Stroke(width = 3f))

        data.forEachIndexed { index, value ->
            val x = (index.toFloat() / (data.size - 1)) * size.width
            val y = size.height - ((value - minVal) / range * size.height * 0.9f).toFloat() - size.height * 0.05f
            drawCircle(lineColor, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
private fun LogMeasurementDialog(
    type: MeasurementType,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log ${type.displayName()}") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { value.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = value.toDoubleOrNull() != null,
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun MeasurementType.displayName(): String = when (this) {
    MeasurementType.WEIGHT -> "Bodyweight"
    MeasurementType.BODY_FAT -> "Body Fat %"
    MeasurementType.CALORIC_INTAKE -> "Caloric Intake"
    MeasurementType.NECK -> "Neck"
    MeasurementType.SHOULDERS -> "Shoulders"
    MeasurementType.CHEST -> "Chest"
    MeasurementType.WAIST -> "Waist"
    MeasurementType.HIPS -> "Hips"
    MeasurementType.LEFT_BICEP -> "Left Bicep"
    MeasurementType.RIGHT_BICEP -> "Right Bicep"
    MeasurementType.LEFT_FOREARM -> "Left Forearm"
    MeasurementType.RIGHT_FOREARM -> "Right Forearm"
    MeasurementType.LEFT_THIGH -> "Left Thigh"
    MeasurementType.RIGHT_THIGH -> "Right Thigh"
    MeasurementType.LEFT_CALF -> "Left Calf"
    MeasurementType.RIGHT_CALF -> "Right Calf"
}
