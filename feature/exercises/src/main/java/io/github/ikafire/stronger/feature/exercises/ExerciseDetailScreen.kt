package io.github.ikafire.stronger.feature.exercises

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.ResistanceProfileType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
) {
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()
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
                1 -> PlaceholderTab("History")
                2 -> PlaceholderTab("Charts")
                3 -> PlaceholderTab("Records")
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

@Composable
private fun PlaceholderTab(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$title - Coming soon",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
