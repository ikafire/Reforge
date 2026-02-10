package io.github.ikafire.stronger.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

data class WarmUpSet(
    val setNumber: Int,
    val percentage: Int,
    val weight: Double,
    val reps: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarmUpCalculatorScreen(
    onBackClick: () -> Unit,
) {
    var workingWeight by remember { mutableStateOf("100") }
    var barWeight by remember { mutableStateOf("20") }

    val warmUpSets by remember(workingWeight, barWeight) {
        derivedStateOf {
            val target = workingWeight.toDoubleOrNull() ?: 0.0
            val bar = barWeight.toDoubleOrNull() ?: 0.0
            calculateWarmUp(target, bar)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Warm-Up Calculator") },
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
                value = workingWeight,
                onValueChange = { workingWeight = it },
                label = { Text("Working Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = barWeight,
                onValueChange = { barWeight = it },
                label = { Text("Bar Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Suggested Warm-Up",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))

            warmUpSets.forEach { set ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (set.percentage == 100)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    ) {
                        Text(
                            text = "Set ${set.setNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${set.percentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${"%.1f".format(set.weight)} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${set.reps} reps",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private fun calculateWarmUp(workingWeight: Double, barWeight: Double): List<WarmUpSet> {
    if (workingWeight <= barWeight) return emptyList()

    val percentages = listOf(
        50 to 10,
        60 to 8,
        70 to 5,
        80 to 3,
        90 to 2,
        100 to -1,
    )

    return percentages.mapIndexed { index, (pct, reps) ->
        val weight = roundToNearest2_5(workingWeight * pct / 100.0, barWeight)
        WarmUpSet(
            setNumber = index + 1,
            percentage = pct,
            weight = weight.coerceAtLeast(barWeight),
            reps = if (reps == -1) 0 else reps,
        )
    }
}

private fun roundToNearest2_5(weight: Double, barWeight: Double): Double {
    val perSide = (weight - barWeight) / 2.0
    val rounded = ceil(perSide / 2.5) * 2.5
    return barWeight + rounded * 2
}
