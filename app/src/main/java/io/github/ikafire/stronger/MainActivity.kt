package io.github.ikafire.stronger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.ikafire.stronger.core.domain.model.ThemeMode
import io.github.ikafire.stronger.core.ui.theme.StrongerTheme
import io.github.ikafire.stronger.navigation.ActiveWorkoutRoute
import io.github.ikafire.stronger.navigation.CreateExerciseRoute
import io.github.ikafire.stronger.navigation.EditExerciseRoute
import io.github.ikafire.stronger.navigation.ExerciseDetailRoute
import io.github.ikafire.stronger.navigation.ExercisesRoute
import io.github.ikafire.stronger.navigation.HistoryRoute
import io.github.ikafire.stronger.navigation.MeasureRoute
import io.github.ikafire.stronger.navigation.ProfileRoute
import io.github.ikafire.stronger.navigation.SettingsRoute
import io.github.ikafire.stronger.navigation.StrongerNavHost
import io.github.ikafire.stronger.navigation.TopLevelDestination
import io.github.ikafire.stronger.navigation.WorkoutRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            StrongerTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StrongerApp()
                }
            }
        }
    }
}

@Composable
fun StrongerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<SettingsRoute>() != true &&
            dest.hasRoute<ExerciseDetailRoute>() != true &&
            dest.hasRoute<CreateExerciseRoute>() != true &&
            dest.hasRoute<EditExerciseRoute>() != true &&
            dest.hasRoute<ActiveWorkoutRoute>() != true
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val route = when (destination) {
                            TopLevelDestination.PROFILE -> ProfileRoute
                            TopLevelDestination.HISTORY -> HistoryRoute
                            TopLevelDestination.WORKOUT -> WorkoutRoute
                            TopLevelDestination.EXERCISES -> ExercisesRoute
                            TopLevelDestination.MEASURE -> MeasureRoute
                        }

                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(route::class)
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        StrongerNavHost(
            navController = navController,
            onNavigateToSettings = {
                navController.navigate(SettingsRoute)
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
