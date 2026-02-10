package io.github.ikafire.reforge.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateCalculatorScreen(
    onBackClick: () -> Unit,
) {
    var targetWeight by remember { mutableStateOf("100") }
    var barWeight by remember { mutableStateOf("20") }

    val availablePlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    val plates by remember(targetWeight, barWeight) {
        derivedStateOf {
            val target = targetWeight.toDoubleOrNull() ?: 0.0
            val bar = barWeight.toDoubleOrNull() ?: 0.0
            calculatePlates(target, bar, availablePlates)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plate Calculator") },
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
                value = targetWeight,
                onValueChange = { targetWeight = it },
                label = { Text("Target Weight (kg)") },
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
                text = "Per Side",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (plates.isEmpty()) {
                Text(
                    text = "No plates needed (bar weight only)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                plates.forEach { (plate, count) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
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
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${plate}kg plate",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            Text(
                                text = "x$count",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val totalPerSide = plates.sumOf { it.first * it.second }
                val totalWeight = (barWeight.toDoubleOrNull() ?: 0.0) + totalPerSide * 2
                Text(
                    text = "Total: ${"%.1f".format(totalWeight)} kg",
                    style = MaterialTheme.typography.titleMedium,
                )

                val target = targetWeight.toDoubleOrNull() ?: 0.0
                if (totalWeight != target && target > 0) {
                    Text(
                        text = "Closest achievable: ${"%.1f".format(totalWeight)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

internal fun calculatePlates(
    targetWeight: Double,
    barWeight: Double,
    availablePlates: List<Double>,
): List<Pair<Double, Int>> {
    var remaining = (targetWeight - barWeight) / 2.0
    if (remaining <= 0) return emptyList()

    val result = mutableListOf<Pair<Double, Int>>()
    for (plate in availablePlates) {
        val count = (remaining / plate).toInt()
        if (count > 0) {
            result.add(plate to count)
            remaining -= plate * count
        }
    }
    return result
}
