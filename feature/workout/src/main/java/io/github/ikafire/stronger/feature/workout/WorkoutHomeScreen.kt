package io.github.ikafire.stronger.feature.workout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHomeScreen(
    onNavigateToActiveWorkout: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
    templateViewModel: TemplateViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val templateUiState by templateViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Workout") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.activeWorkout != null) {
                Button(
                    onClick = onNavigateToActiveWorkout,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continue Workout")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Quick Start",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (uiState.activeWorkout != null) {
                        onNavigateToActiveWorkout()
                    } else {
                        viewModel.startEmptyWorkout(onStarted = onNavigateToActiveWorkout)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Start Empty Workout")
            }

            Spacer(modifier = Modifier.height(24.dp))

            TemplateListSection(
                uiState = templateUiState,
                onTemplateClick = { /* TODO: navigate to template preview */ },
                onStartFromTemplate = { templateId ->
                    templateViewModel.startWorkoutFromTemplate(templateId)
                    onNavigateToActiveWorkout()
                },
                onDeleteTemplate = templateViewModel::deleteTemplate,
                onDuplicateTemplate = templateViewModel::duplicateTemplate,
                onCreateTemplate = templateViewModel::createTemplate,
                onCreateFolder = templateViewModel::createFolder,
                onDeleteFolder = templateViewModel::deleteFolder,
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
