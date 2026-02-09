package io.github.ikafire.stronger.feature.exercises

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ikafire.stronger.core.domain.model.ExerciseCategory
import io.github.ikafire.stronger.core.domain.model.ExerciseWithUsage
import io.github.ikafire.stronger.core.domain.model.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    onExerciseClick: (String) -> Unit,
    onCreateExerciseClick: () -> Unit,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder = { Text("Search exercises...") },
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
                        Text("Exercises")
                    }
                },
                actions = {
                    if (showSearch) {
                        IconButton(onClick = {
                            showSearch = false
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    } else {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateExerciseClick) {
                Icon(Icons.Default.Add, contentDescription = "Create exercise")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            FilterBar(
                selectedCategory = uiState.selectedCategory,
                selectedMuscle = uiState.selectedMuscle,
                onCategorySelected = viewModel::onCategorySelected,
                onMuscleSelected = viewModel::onMuscleSelected,
            )

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Loading exercises...")
                }
            } else {
                ExerciseGroupedList(
                    groupedExercises = uiState.groupedExercises,
                    onExerciseClick = onExerciseClick,
                )
            }
        }
    }
}

@Composable
private fun FilterBar(
    selectedCategory: ExerciseCategory?,
    selectedMuscle: MuscleGroup?,
    onCategorySelected: (ExerciseCategory?) -> Unit,
    onMuscleSelected: (MuscleGroup?) -> Unit,
) {
    var showCategoryFilter by remember { mutableStateOf(false) }
    var showMuscleFilter by remember { mutableStateOf(false) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedCategory != null,
                onClick = { showCategoryFilter = !showCategoryFilter },
                label = {
                    Text(selectedCategory?.displayName() ?: "Equipment")
                },
            )
        }
        item {
            FilterChip(
                selected = selectedMuscle != null,
                onClick = { showMuscleFilter = !showMuscleFilter },
                label = {
                    Text(selectedMuscle?.displayName() ?: "Muscle")
                },
            )
        }
        if (selectedCategory != null || selectedMuscle != null) {
            item {
                FilterChip(
                    selected = false,
                    onClick = {
                        onCategorySelected(null)
                        onMuscleSelected(null)
                    },
                    label = { Text("Clear") },
                )
            }
        }
    }

    if (showCategoryFilter) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ExerciseCategory.entries.toList()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(if (selectedCategory == category) null else category)
                        showCategoryFilter = false
                    },
                    label = { Text(category.displayName()) },
                )
            }
        }
    }

    if (showMuscleFilter) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(MuscleGroup.entries.toList()) { muscle ->
                FilterChip(
                    selected = selectedMuscle == muscle,
                    onClick = {
                        onMuscleSelected(if (selectedMuscle == muscle) null else muscle)
                        showMuscleFilter = false
                    },
                    label = { Text(muscle.displayName()) },
                )
            }
        }
    }
}

@Composable
private fun ExerciseGroupedList(
    groupedExercises: Map<UsageGroup, List<ExerciseWithUsage>>,
    onExerciseClick: (String) -> Unit,
) {
    if (groupedExercises.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "No exercises found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedExercises.forEach { (group, exercises) ->
            item {
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(exercises, key = { it.exercise.id }) { item ->
                ExerciseListItem(
                    exerciseWithUsage = item,
                    onClick = { onExerciseClick(item.exercise.id) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun ExerciseListItem(
    exerciseWithUsage: ExerciseWithUsage,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exerciseWithUsage.exercise.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = exerciseWithUsage.exercise.primaryMuscle.displayName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${exerciseWithUsage.usageCount}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun ExerciseCategory.displayName(): String = when (this) {
    ExerciseCategory.BARBELL -> "Barbell"
    ExerciseCategory.DUMBBELL -> "Dumbbell"
    ExerciseCategory.CABLE -> "Cable"
    ExerciseCategory.MACHINE -> "Machine"
    ExerciseCategory.BODYWEIGHT -> "Bodyweight"
    ExerciseCategory.CARDIO -> "Cardio"
    ExerciseCategory.DURATION -> "Duration"
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
