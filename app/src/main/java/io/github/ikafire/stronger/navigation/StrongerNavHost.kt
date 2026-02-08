package io.github.ikafire.stronger.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.ikafire.stronger.feature.settings.SettingsScreen

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
            PlaceholderScreen(title = "Workout")
        }
        composable<ExercisesRoute> {
            PlaceholderScreen(title = "Exercises")
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
