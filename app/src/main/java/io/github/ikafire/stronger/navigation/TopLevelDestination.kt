package io.github.ikafire.stronger.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
) {
    PROFILE(
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        label = "Profile",
    ),
    HISTORY(
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        label = "History",
    ),
    WORKOUT(
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter,
        label = "Workout",
    ),
    EXERCISES(
        selectedIcon = Icons.AutoMirrored.Filled.List,
        unselectedIcon = Icons.AutoMirrored.Outlined.List,
        label = "Exercises",
    ),
    MEASURE(
        selectedIcon = Icons.Filled.MonitorWeight,
        unselectedIcon = Icons.Outlined.MonitorWeight,
        label = "Measure",
    ),
}

// Type-safe route objects
@Serializable object ProfileRoute
@Serializable object HistoryRoute
@Serializable object WorkoutRoute
@Serializable object ExercisesRoute
@Serializable object MeasureRoute
@Serializable object SettingsRoute
@Serializable data class ExerciseDetailRoute(val exerciseId: String)
@Serializable object CreateExerciseRoute
@Serializable data class EditExerciseRoute(val exerciseId: String)
@Serializable object ActiveWorkoutRoute
@Serializable object AnalyticsRoute
@Serializable object PlateCalculatorRoute
@Serializable object WarmUpCalculatorRoute
