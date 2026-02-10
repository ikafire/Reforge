package io.github.ikafire.reforge.feature.exercises

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    onBackClick: () -> Unit,
    viewModel: CreateExerciseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edit Exercise" else "Create Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Exercise Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            EnumDropdown(
                label = "Equipment",
                selected = uiState.category,
                options = ExerciseCategory.entries.toList(),
                displayName = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelected = viewModel::onCategoryChange,
            )

            Spacer(modifier = Modifier.height(16.dp))

            EnumDropdown(
                label = "Primary Muscle",
                selected = uiState.primaryMuscle,
                options = MuscleGroup.entries.toList(),
                displayName = { it.name.lowercase().replace("_", " ").replaceFirstChar { c -> c.uppercase() } },
                onSelected = viewModel::onPrimaryMuscleChange,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Secondary Muscles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            MuscleGroup.entries.forEach { muscle ->
                if (muscle != uiState.primaryMuscle) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = muscle in uiState.secondaryMuscles,
                            onCheckedChange = { checked ->
                                val updated = if (checked) {
                                    uiState.secondaryMuscles + muscle
                                } else {
                                    uiState.secondaryMuscles - muscle
                                }
                                viewModel.onSecondaryMusclesChange(updated)
                            },
                        )
                        Text(
                            text = muscle.name.lowercase().replace("_", " ")
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.instructions,
                onValueChange = viewModel::onInstructionsChange,
                label = { Text("Instructions (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                enabled = uiState.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isEditing) "Save Changes" else "Create Exercise")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    selected: T,
    options: List<T>,
    displayName: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = displayName(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayName(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
