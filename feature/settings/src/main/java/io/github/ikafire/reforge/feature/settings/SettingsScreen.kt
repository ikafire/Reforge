package io.github.ikafire.reforge.feature.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ikafire.reforge.core.domain.model.LengthUnit
import io.github.ikafire.reforge.core.domain.model.ThemeMode
import io.github.ikafire.reforge.core.domain.model.WeightUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onPlateCalculatorClick: () -> Unit = {},
    onWarmUpCalculatorClick: () -> Unit = {},
    onImportCsv: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var pendingWeightUnit by remember { mutableStateOf<WeightUnit?>(null) }
    var pendingLengthUnit by remember { mutableStateOf<LengthUnit?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Units section
            SectionHeader("Units")

            SettingsLabel("Weight")
            RadioOption(
                label = "Kilograms (kg)",
                selected = preferences.weightUnit == WeightUnit.KG,
                onClick = {
                    if (preferences.weightUnit != WeightUnit.KG) {
                        pendingWeightUnit = WeightUnit.KG
                    }
                },
            )
            RadioOption(
                label = "Pounds (lbs)",
                selected = preferences.weightUnit == WeightUnit.LBS,
                onClick = {
                    if (preferences.weightUnit != WeightUnit.LBS) {
                        pendingWeightUnit = WeightUnit.LBS
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsLabel("Measurements")
            RadioOption(
                label = "Centimeters (cm)",
                selected = preferences.lengthUnit == LengthUnit.CM,
                onClick = {
                    if (preferences.lengthUnit != LengthUnit.CM) {
                        pendingLengthUnit = LengthUnit.CM
                    }
                },
            )
            RadioOption(
                label = "Inches (in)",
                selected = preferences.lengthUnit == LengthUnit.IN,
                onClick = {
                    if (preferences.lengthUnit != LengthUnit.IN) {
                        pendingLengthUnit = LengthUnit.IN
                    }
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Timer section
            SectionHeader("Rest Timer")
            SettingsLabel("Default Rest Duration")
            val timerOptions = listOf(30, 60, 90, 120, 180, 300)
            timerOptions.forEach { seconds ->
                val label = if (seconds < 60) "${seconds}s" else "${seconds / 60}m${if (seconds % 60 > 0) " ${seconds % 60}s" else ""}"
                RadioOption(
                    label = label,
                    selected = preferences.defaultRestTimerSeconds == seconds,
                    onClick = { viewModel.setDefaultRestTimerSeconds(seconds) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Theme section
            SectionHeader("Theme")
            RadioOption(
                label = "System",
                selected = preferences.themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
            )
            RadioOption(
                label = "Light",
                selected = preferences.themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
            )
            RadioOption(
                label = "Dark",
                selected = preferences.themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Tools section
            SectionHeader("Tools")
            SettingsNavItem(label = "Plate Calculator", onClick = onPlateCalculatorClick)
            SettingsNavItem(label = "Warm-Up Calculator", onClick = onWarmUpCalculatorClick)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Data section
            SectionHeader("Data")
            SettingsNavItem(label = "Import Strong CSV", onClick = onImportCsv)
            SettingsNavItem(label = "Export CSV", onClick = onExportCsv)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Backup section
            SectionHeader("Backup & Restore")
            Text(
                text = "Google Drive backup coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }

    // Weight unit conversion dialog
    pendingWeightUnit?.let { newUnit ->
        val unitLabel = if (newUnit == WeightUnit.KG) "kg" else "lbs"
        AlertDialog(
            onDismissRequest = { pendingWeightUnit = null },
            title = { Text("Change Weight Unit") },
            text = { Text("Would you like to convert all existing weight data to $unitLabel?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setWeightUnit(newUnit, convertData = true)
                    pendingWeightUnit = null
                }) {
                    Text("Convert All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setWeightUnit(newUnit, convertData = false)
                    pendingWeightUnit = null
                }) {
                    Text("Just Change Unit")
                }
            },
        )
    }

    // Length unit conversion dialog
    pendingLengthUnit?.let { newUnit ->
        val unitLabel = if (newUnit == LengthUnit.CM) "cm" else "in"
        AlertDialog(
            onDismissRequest = { pendingLengthUnit = null },
            title = { Text("Change Measurement Unit") },
            text = { Text("Would you like to convert all existing measurement data to $unitLabel?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setLengthUnit(newUnit, convertData = true)
                    pendingLengthUnit = null
                }) {
                    Text("Convert All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setLengthUnit(newUnit, convertData = false)
                    pendingLengthUnit = null
                }) {
                    Text("Just Change Unit")
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingsLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
private fun SettingsNavItem(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun RadioOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
