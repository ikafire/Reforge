package io.github.ikafire.stronger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.repository.ExerciseRepository
import io.github.ikafire.stronger.feature.exercises.CreateExerciseScreen
import io.github.ikafire.stronger.feature.exercises.ExerciseDetailScreen
import io.github.ikafire.stronger.feature.exercises.ExerciseListScreen
import io.github.ikafire.stronger.feature.settings.SettingsScreen
import io.github.ikafire.stronger.feature.workout.ActiveWorkoutScreen
import io.github.ikafire.stronger.feature.workout.WorkoutHomeScreen
import io.github.ikafire.stronger.feature.workout.WorkoutViewModel

@Composable
fun StrongerNavHost(
    navController: NavHostController,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = WorkoutRoute,
        modifier = modifier,
    ) {
        composable<ProfileRoute> {
            PlaceholderScreen(title = "Profile", onSettingsClick = onNavigateToSettings)
        }
        composable<HistoryRoute> {
            PlaceholderScreen(title = "History")
        }
        composable<WorkoutRoute> {
            val workoutViewModel: WorkoutViewModel = hiltViewModel()
            WorkoutHomeScreen(
                onNavigateToActiveWorkout = {
                    navController.navigate(ActiveWorkoutRoute)
                },
                viewModel = workoutViewModel,
            )
        }
        composable<ActiveWorkoutRoute> {
            val workoutViewModel: WorkoutViewModel = hiltViewModel()
            val exerciseListViewModel: io.github.ikafire.stronger.feature.exercises.ExerciseListViewModel = hiltViewModel()
            val exerciseUiState by exerciseListViewModel.uiState.collectAsStateWithLifecycle()
            val allExercises = exerciseUiState.groupedExercises.values.flatten().map { it.exercise }

            ActiveWorkoutScreen(
                onBackClick = { navController.popBackStack() },
                allExercises = allExercises,
                viewModel = workoutViewModel,
            )
        }
        composable<ExercisesRoute> {
            ExerciseListScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigate(ExerciseDetailRoute(exerciseId))
                },
                onCreateExerciseClick = {
                    navController.navigate(CreateExerciseRoute)
                },
            )
        }
        composable<ExerciseDetailRoute> {
            ExerciseDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { exerciseId ->
                    navController.navigate(EditExerciseRoute(exerciseId))
                },
            )
        }
        composable<CreateExerciseRoute> {
            CreateExerciseScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<EditExerciseRoute> {
            CreateExerciseScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<MeasureRoute> {
            PlaceholderScreen(title = "Measure")
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
