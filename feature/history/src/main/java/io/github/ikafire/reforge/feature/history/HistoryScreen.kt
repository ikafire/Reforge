package io.github.ikafire.reforge.feature.history

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }
    var deleteWorkoutId by remember { mutableStateOf<String?>(null) }

    // If a workout is selected, show detail
    uiState.selectedWorkout?.let { detail ->
        WorkoutDetailScreen(
            detail = detail,
            onBack = { viewModel.clearSelectedWorkout() },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Search history...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text("History")
                    }
                },
                actions = {
                    if (showSearch) {
                        IconButton(onClick = {
                            showSearch = false
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleCalendar() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                        }
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.workoutsByMonth.isEmpty() && !uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No workout history yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                uiState.workoutsByMonth.forEach { (month, workouts) ->
                    item {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(workouts, key = { it.workout.id }) { summary ->
                        WorkoutCard(
                            summary = summary,
                            onClick = { viewModel.selectWorkout(summary.workout.id) },
                            onDelete = { deleteWorkoutId = summary.workout.id },
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Delete confirmation
    deleteWorkoutId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteWorkoutId = null },
            title = { Text("Delete Workout?") },
            text = { Text("This workout will be permanently deleted from history.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWorkout(id)
                    deleteWorkoutId = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteWorkoutId = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Calendar dialog
    if (uiState.showCalendar) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCalendar() },
            title = { Text("Workout Calendar") },
            text = {
                CalendarView(workoutDates = uiState.workoutDates)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideCalendar() }) {
                    Text("Close")
                }
            },
        )
    }
}

@Composable
private fun CalendarView(
    workoutDates: Set<LocalDate>,
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentMonth = today.month
    val currentYear = today.year

    // Show current month and previous month
    Column {
        listOf(0, -1, -2).forEach { offset ->
            val monthDate = today.minus(-offset, DateTimeUnit.MONTH)
            val year = if (offset == 0) currentYear else {
                val m = currentMonth.ordinal + 1 + offset
                if (m <= 0) currentYear - 1 else currentYear
            }
            MonthCalendar(
                year = monthDate.year,
                month = monthDate.monthNumber,
                workoutDates = workoutDates,
                today = today,
            )
            if (offset != -2) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    year: Int,
    month: Int,
    workoutDates: Set<LocalDate>,
    today: LocalDate,
) {
    val firstDay = LocalDate(year, month, 1)
    val daysInMonth = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    val monthName = firstDay.month.name.lowercase().replaceFirstChar { it.uppercase() }

    Column {
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Calendar grid
        val startDayOfWeek = firstDay.dayOfWeek.ordinal // Monday = 0
        var dayCounter = 1

        for (week in 0..5) {
            if (dayCounter > daysInMonth) break
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dow in 0..6) {
                    val cellIndex = week * 7 + dow
                    if (cellIndex < startDayOfWeek || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(28.dp))
                    } else {
                        val date = LocalDate(year, month, dayCounter)
                        val hasWorkout = date in workoutDates
                        val isToday = date == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .then(
                                    if (hasWorkout) Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$dayCounter",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    hasWorkout -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailScreen(
    detail: WorkoutDetailData,
    onBack: () -> Unit,
) {
    val date = detail.workout.startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val duration = detail.workout.finishedAt?.let {
        val secs = (it - detail.workout.startedAt).inWholeSeconds.toInt()
        val m = secs / 60
        val s = secs % 60
        "${m}m ${s}s"
    } ?: "In progress"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail.workout.name ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = "${date.dayOfMonth}/${date.monthNumber}/${date.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Duration: $duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                detail.workout.notes?.let { notes ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
            }

            items(detail.exercises) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = exercise.exerciseName,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Header row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("SET", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("WEIGHT", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("REPS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("RPE", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        exercise.sets.forEachIndexed { index, set ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text("${index + 1}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
                                Text("${set.weight ?: "-"} kg", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text("${set.reps ?: "-"}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(set.rpe?.toString() ?: "-", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    summary: WorkoutSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val date = summary.workout.startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val duration = summary.workout.finishedAt?.let {
        val secs = (it - summary.workout.startedAt).inWholeSeconds.toInt()
        val m = secs / 60
        val s = secs % 60
        "${m}m ${s}s"
    } ?: "In progress"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
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
                        text = summary.workout.name ?: "Workout",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "${date.dayOfMonth}/${date.monthNumber}/${date.year} - $duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            summary.exerciseNames.take(5).forEach { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (summary.exerciseNames.size > 5) {
                Text(
                    text = "+${summary.exerciseNames.size - 5} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${summary.totalSets} sets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "%.0f kg".format(summary.totalVolume),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
