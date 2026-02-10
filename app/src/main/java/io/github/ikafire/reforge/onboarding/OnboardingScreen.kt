package io.github.ikafire.reforge.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ikafire.reforge.core.domain.model.LengthUnit
import io.github.ikafire.reforge.core.domain.model.WeightUnit

@Composable
fun OnboardingScreen(
    onComplete: (WeightUnit, LengthUnit) -> Unit,
    onSkip: () -> Unit,
    onImportCsv: () -> Unit = {},
) {
    var step by remember { mutableIntStateOf(0) }
    var weightUnit by remember { mutableStateOf(WeightUnit.KG) }
    var lengthUnit by remember { mutableStateOf(LengthUnit.CM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (step) {
            0 -> {
                Text(
                    text = "Welcome to Reforge",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Track your workouts, measure progress, and get stronger.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { step = 1 },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Get Started")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onSkip) {
                    Text("Skip")
                }
            }
            1 -> {
                Text(
                    text = "Weight Unit",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "How do you measure weight?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))

                UnitOption(
                    label = "Kilograms (kg)",
                    selected = weightUnit == WeightUnit.KG,
                    onClick = { weightUnit = WeightUnit.KG },
                )
                UnitOption(
                    label = "Pounds (lbs)",
                    selected = weightUnit == WeightUnit.LBS,
                    onClick = { weightUnit = WeightUnit.LBS },
                )

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next")
                }
            }
            2 -> {
                Text(
                    text = "Measurement Unit",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "How do you measure body parts?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))

                UnitOption(
                    label = "Centimeters (cm)",
                    selected = lengthUnit == LengthUnit.CM,
                    onClick = { lengthUnit = LengthUnit.CM },
                )
                UnitOption(
                    label = "Inches (in)",
                    selected = lengthUnit == LengthUnit.IN,
                    onClick = { lengthUnit = LengthUnit.IN },
                )

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { step = 3 },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next")
                }
            }
            3 -> {
                Text(
                    text = "Import Data",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Have data from the Strong app? Import your workout history.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onImportCsv,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Import Strong CSV")
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onComplete(weightUnit, lengthUnit) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Finish Setup")
                }
            }
        }
    }
}

@Composable
private fun UnitOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
