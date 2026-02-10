package io.github.ikafire.stronger.feature.workout

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.SetType
import io.github.ikafire.stronger.core.domain.model.WorkoutSet
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onBackClick: () -> Unit,
    allExercises: List<Exercise>,
    viewModel: WorkoutViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val workout = uiState.activeWorkout

    if (workout == null && !uiState.isLoading) {
        LaunchedEffect(Unit) { onBackClick() }
        return
    }

    if (workout == null) {
        // Still loading — wait for DB to populate
        return
    }

    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(workout.startedAt) {
        while (true) {
            elapsedSeconds = (Clock.System.now() - workout.startedAt).inWholeSeconds.toInt()
            delay(1000)
        }
    }

    if (uiState.showExercisePicker) {
        io.github.ikafire.stronger.feature.exercises.ExercisePickerDialog(
            exercises = allExercises,
            onExercisesSelected = { viewModel.addExercises(it) },
            onDismiss = { viewModel.hideExercisePicker() },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(workout.name ?: "Workout", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatDuration(elapsedSeconds),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.showFinishDialog() }) {
                        Text("Finish")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                WorkoutNotesSection(
                    notes = workout.notes ?: "",
                    onNotesChange = viewModel::updateWorkoutNotes,
                )
            }

            items(uiState.exercises, key = { it.workoutExercise.id }) { detail ->
                ExerciseCard(
                    detail = detail,
                    onAddSet = { viewModel.addSet(detail.workoutExercise.id) },
                    onRemoveSet = viewModel::removeSet,
                    onUpdateSet = viewModel::updateSet,
                    onCompleteSet = viewModel::completeSet,
                    onUncompleteSet = viewModel::uncompleteSet,
                    onSetType = viewModel::setSetType,
                    onRemoveExercise = { viewModel.removeExercise(detail.workoutExercise.id) },
                    onExerciseNotesChange = { notes ->
                        viewModel.updateExerciseNotes(detail.workoutExercise, notes)
                    },
                    onRestTimerChange = { seconds ->
                        viewModel.updateExerciseRestTimer(detail.workoutExercise, seconds)
                    },
                )
            }

            item {
                Button(
                    onClick = { viewModel.showExercisePicker() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Exercise")
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    OutlinedButton(
                        onClick = { viewModel.showDiscardDialog() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    ) {
                        Text("Discard Workout")
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDiscardDialog() },
            title = { Text("Discard Workout?") },
            text = { Text("All logged data will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = { viewModel.discardWorkout() }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDiscardDialog() }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (uiState.showFinishDialog) {
        val hasIncomplete = uiState.exercises.any { detail ->
            detail.sets.any { !it.isCompleted }
        }
        AlertDialog(
            onDismissRequest = { viewModel.hideFinishDialog() },
            title = { Text("Finish Workout?") },
            text = {
                if (hasIncomplete) {
                    Text("Some sets are not completed. Incomplete sets will be discarded.")
                } else {
                    Text("Save this workout to history?")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.finishWorkout(discardIncomplete = true) }) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideFinishDialog() }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun WorkoutNotesSection(
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(notes.isNotBlank()) }

    if (expanded) {
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Workout notes") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            minLines = 2,
        )
    } else {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text("Add workout note")
        }
    }
}

@Composable
private fun ExerciseCard(
    detail: WorkoutExerciseWithDetails,
    onAddSet: () -> Unit,
    onRemoveSet: (String) -> Unit,
    onUpdateSet: (WorkoutSet) -> Unit,
    onCompleteSet: (WorkoutSet) -> Unit,
    onUncompleteSet: (WorkoutSet) -> Unit,
    onSetType: (WorkoutSet, SetType) -> Unit,
    onRemoveExercise: () -> Unit,
    onExerciseNotesChange: (String) -> Unit,
    onRestTimerChange: (Int?) -> Unit,
) {
    val exercise = detail.exercise
    val supersetGroup = detail.workoutExercise.supersetGroup
    var showMenu by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(detail.workoutExercise.notes?.isNotBlank() == true) }
    var showRestTimerDialog by remember { mutableStateOf(false) }

    val totalReps = detail.sets.filter { it.isCompleted }.sumOf { it.reps ?: 0 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        border = if (supersetGroup != null) BorderStroke(2.dp, Color(0xFFFFA500)) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise?.name ?: "Unknown Exercise",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (totalReps > 0) {
                        Text(
                            text = "$totalReps reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Add Note") },
                            onClick = {
                                showNotes = true
                                showMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = {
                                val current = detail.workoutExercise.restTimerSeconds
                                Text(if (current != null) "Rest Timer (${current}s)" else "Rest Timer")
                            },
                            onClick = {
                                showRestTimerDialog = true
                                showMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Exercise") },
                            onClick = {
                                showMenu = false
                                if (detail.sets.any { it.isCompleted }) {
                                    showRemoveDialog = true
                                } else {
                                    onRemoveExercise()
                                }
                            },
                        )
                    }
                }
            }

            if (showNotes) {
                OutlinedTextField(
                    value = detail.workoutExercise.notes ?: "",
                    onValueChange = onExerciseNotesChange,
                    label = { Text("Exercise note") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("SET", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Text("PREVIOUS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("KG", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(64.dp), textAlign = TextAlign.Center)
                Text("REPS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(64.dp), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(40.dp))
            }

            // Set rows
            detail.sets.forEachIndexed { index, set ->
                SetRow(
                    set = set,
                    index = index,
                    previousSet = detail.previousSets.getOrNull(index),
                    onUpdate = onUpdateSet,
                    onComplete = onCompleteSet,
                    onUncomplete = onUncompleteSet,
                    onSetType = onSetType,
                    onRemove = { onRemoveSet(set.id) },
                )
            }

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("+ Add Set")
            }
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Exercise?") },
            text = { Text("This exercise has completed sets. Remove it and all its data?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveExercise()
                    showRemoveDialog = false
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showRestTimerDialog) {
        val options = listOf(null, 30, 60, 90, 120, 180, 300)
        AlertDialog(
            onDismissRequest = { showRestTimerDialog = false },
            title = { Text("Rest Timer") },
            text = {
                Column {
                    Text("Select rest duration for this exercise:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    options.forEach { seconds ->
                        val label = when (seconds) {
                            null -> "Default"
                            else -> "${seconds / 60}m ${seconds % 60}s".replace("0m ", "").replace(" 0s", "")
                        }
                        val selected = detail.workoutExercise.restTimerSeconds == seconds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRestTimerChange(seconds)
                                    showRestTimerDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                            if (selected) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
private fun SetRow(
    set: WorkoutSet,
    index: Int,
    previousSet: WorkoutSet?,
    onUpdate: (WorkoutSet) -> Unit,
    onComplete: (WorkoutSet) -> Unit,
    onUncomplete: (WorkoutSet) -> Unit,
    onSetType: (WorkoutSet, SetType) -> Unit,
    onRemove: () -> Unit,
) {
    var showTypeMenu by remember { mutableStateOf(false) }
    val setLabel = when (set.type) {
        SetType.WARMUP -> "W"
        SetType.WORKING -> "${index + 1}"
        SetType.FAILURE -> "F"
        SetType.DROP -> "D"
    }

    val completedBg = if (set.isCompleted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(completedBg)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Set number / type
        Box(modifier = Modifier.width(36.dp)) {
            Text(
                text = setLabel,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTypeMenu = true },
            )
            DropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                SetType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onSetType(set, type)
                            showTypeMenu = false
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text("Remove Set") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = {
                        onRemove()
                        showTypeMenu = false
                    },
                )
            }
        }

        // Previous
        Text(
            text = previousSet?.let { "${it.weight ?: "-"} x ${it.reps ?: "-"}" } ?: "-",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        // Weight input
        OutlinedTextField(
            value = set.weight?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "",
            onValueChange = { text ->
                val weight = text.toDoubleOrNull()
                onUpdate(set.copy(weight = weight))
            },
            modifier = Modifier.width(64.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
        )

        // Reps input
        OutlinedTextField(
            value = set.reps?.toString() ?: "",
            onValueChange = { text ->
                val reps = text.toIntOrNull()
                onUpdate(set.copy(reps = reps))
            },
            modifier = Modifier.width(64.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
        )

        // Complete/uncomplete button
        IconButton(
            onClick = {
                if (set.isCompleted) onUncomplete(set)
                else onComplete(set)
            },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (set.isCompleted) "Undo" else "Complete",
                tint = if (set.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
