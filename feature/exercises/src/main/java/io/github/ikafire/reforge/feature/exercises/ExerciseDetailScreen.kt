package io.github.ikafire.reforge.feature.exercises

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseHistoryItem
import io.github.ikafire.reforge.core.domain.model.ResistanceProfileType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val oneRmData by viewModel.oneRmData.collectAsStateWithLifecycle()
    val volumeData by viewModel.volumeData.collectAsStateWithLifecycle()
    val bestSetData by viewModel.bestSetData.collectAsStateWithLifecycle()
    val totalRepsData by viewModel.totalRepsData.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("About", "History", "Charts", "Records")
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (exercise == null) {
        return
    }

    val ex = exercise!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ex.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (ex.isCustom) {
                        IconButton(onClick = { onEditClick(ex.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            when (selectedTab) {
                0 -> AboutTab(
                    exercise = ex,
                    onSetResistanceProfile = { type, multiplier, notes ->
                        viewModel.setResistanceProfile(type, multiplier, notes)
                    },
                    onClearResistanceProfile = viewModel::clearResistanceProfile,
                )
                1 -> HistoryTab(history = history)
                2 -> ChartsTab(
                    oneRmData = oneRmData,
                    volumeData = volumeData,
                    bestSetData = bestSetData,
                    totalRepsData = totalRepsData,
                )
                3 -> RecordsTab(records = records, history = history)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Exercise") },
            text = { Text("Are you sure you want to delete \"${ex.name}\"? Workout history will retain the exercise name but it will be removed from the library.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExercise()
                    showDeleteDialog = false
                    onBackClick()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun HistoryTab(history: List<ExerciseHistoryItem>) {
    if (history.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No history yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(history, key = { it.workoutId }) { item ->
            val date = item.workoutDate.toLocalDateTime(TimeZone.currentSystemDefault())
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = item.workoutName ?: "Workout",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "${date.dayOfMonth}/${date.monthNumber}/${date.year}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    item.sets.forEachIndexed { index, set ->
                        val setInfo = buildString {
                            append("Set ${index + 1}: ")
                            if (set.weight != null) append("${set.weight} kg")
                            if (set.reps != null) {
                                if (set.weight != null) append(" x ")
                                append("${set.reps} reps")
                            }
                            if (set.rpe != null) append(" @${set.rpe}")
                        }
                        Text(
                            text = setInfo,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartsTab(
    oneRmData: List<OneRmDataPoint>,
    volumeData: List<ChartDataPoint>,
    bestSetData: List<ChartDataPoint>,
    totalRepsData: List<ChartDataPoint>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            ChartSection(
                title = "Estimated 1RM (Epley)",
                data = oneRmData.map { it.oneRm },
                unit = "kg",
            )
        }
        item {
            ChartSection(
                title = "Total Volume",
                data = volumeData.map { it.value },
                unit = "kg",
            )
        }
        item {
            ChartSection(
                title = "Best Set (Weight)",
                data = bestSetData.map { it.value },
                unit = "kg",
            )
        }
        item {
            ChartSection(
                title = "Total Reps",
                data = totalRepsData.map { it.value },
                unit = "reps",
            )
        }
    }
}

@Composable
private fun ChartSection(
    title: String,
    data: List<Double>,
    unit: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (data.size < 2) {
        Text(
            text = "Need at least 2 workouts to show chart",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
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

    Spacer(modifier = Modifier.height(4.dp))

    val latest = data.lastOrNull()
    val best = data.maxOrNull()
    if (latest != null) {
        Text(
            text = "Latest: ${"%.1f".format(latest)} $unit",
            style = MaterialTheme.typography.bodySmall,
        )
    }
    if (best != null) {
        Text(
            text = "Best: ${"%.1f".format(best)} $unit",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecordsTab(records: ExerciseRecords, history: List<ExerciseHistoryItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        item {
            Text(
                text = "Personal Records",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            RecordCard("Max Weight", records.maxWeight?.let { "${"%.1f".format(it)} kg" })
            RecordCard("Max Reps", records.maxReps?.toString())
            RecordCard("Max Est. 1RM", records.maxOneRm?.let { "${"%.1f".format(it)} kg" })
            RecordCard("Max Session Volume", records.maxVolume?.let { "${"%.0f".format(it)} kg" })

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lifetime Stats",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            StatRow("Total Sets", records.totalSets.toString())
            StatRow("Total Reps", records.totalReps.toString())
            StatRow("Total Volume", "${"%.0f".format(records.totalVolume)} kg")
        }

        // Records History - chronological list of PRs
        if (history.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Records History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val prHistory = buildPrHistory(history)
            items(prHistory, key = { "${it.date}-${it.type}" }) { pr ->
                val date = pr.date.toLocalDateTime(TimeZone.currentSystemDefault())
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(pr.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(pr.value, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "${date.dayOfMonth}/${date.monthNumber}/${date.year}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCard(label: String, value: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = value ?: "-",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutTab(
    exercise: Exercise,
    onSetResistanceProfile: (ResistanceProfileType, Double, String?) -> Unit,
    onClearResistanceProfile: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        DetailRow("Name", exercise.name)
        DetailRow("Equipment", exercise.category.name.lowercase().replaceFirstChar { it.uppercase() })
        DetailRow("Primary Muscle", exercise.primaryMuscle.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })

        if (exercise.secondaryMuscles.isNotEmpty()) {
            DetailRow(
                "Secondary Muscles",
                exercise.secondaryMuscles.joinToString(", ") {
                    it.name.lowercase().replace("_", " ").replaceFirstChar { c -> c.uppercase() }
                },
            )
        }

        val instructions = exercise.instructions
        if (instructions != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = instructions,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Resistance Profile",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))

        val rp = exercise.resistanceProfile
        if (rp != null) {
            DetailRow("Type", rp.type.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailRow("Multiplier", rp.multiplier.toString())
            val rpNotes = rp.notes
            if (rpNotes != null) {
                DetailRow("Notes", rpNotes)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onClearResistanceProfile) {
                Text("Remove Resistance Profile")
            }
        } else {
            Text(
                text = "No resistance profile configured. Effective weight equals loaded weight.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ResistanceProfileForm(onSave = onSetResistanceProfile)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResistanceProfileForm(
    onSave: (ResistanceProfileType, Double, String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(ResistanceProfileType.DIRECT) }
    var multiplier by remember { mutableDoubleStateOf(1.0) }
    var notes by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedType.name.lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Profile Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ResistanceProfileType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        selectedType = type
                        expanded = false
                    },
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = multiplier.toString(),
        onValueChange = { multiplier = it.toDoubleOrNull() ?: multiplier },
        label = { Text("Multiplier") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notes (optional)") },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = { onSave(selectedType, multiplier, notes.ifBlank { null }) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Set Resistance Profile")
    }
}

internal data class PrRecord(
    val date: Instant,
    val type: String,
    val value: String,
)

internal fun buildPrHistory(history: List<ExerciseHistoryItem>): List<PrRecord> {
    val sorted = history.sortedBy { it.workoutDate }
    val records = mutableListOf<PrRecord>()
    var bestWeight = 0.0
    var bestReps = 0
    var bestVolume = 0.0

    for (item in sorted) {
        val maxWeight = item.sets.maxOfOrNull { it.weight ?: 0.0 } ?: 0.0
        val maxReps = item.sets.maxOfOrNull { it.reps ?: 0 } ?: 0
        val volume = item.sets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }

        if (maxWeight > bestWeight && maxWeight > 0) {
            bestWeight = maxWeight
            records.add(PrRecord(item.workoutDate, "Weight PR", "${"%.1f".format(maxWeight)} kg"))
        }
        if (maxReps > bestReps && maxReps > 0) {
            bestReps = maxReps
            records.add(PrRecord(item.workoutDate, "Reps PR", "$maxReps reps"))
        }
        if (volume > bestVolume && volume > 0) {
            bestVolume = volume
            records.add(PrRecord(item.workoutDate, "Volume PR", "${"%.0f".format(volume)} kg"))
        }
    }

    return records.sortedByDescending { it.date }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
