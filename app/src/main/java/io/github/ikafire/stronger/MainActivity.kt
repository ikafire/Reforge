package io.github.ikafire.stronger

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.ikafire.stronger.core.data.sync.CsvExporter
import io.github.ikafire.stronger.core.data.sync.CsvImporter
import io.github.ikafire.stronger.core.domain.model.ThemeMode
import io.github.ikafire.stronger.core.ui.theme.StrongerTheme
import io.github.ikafire.stronger.navigation.ActiveWorkoutRoute
import io.github.ikafire.stronger.navigation.AnalyticsRoute
import io.github.ikafire.stronger.navigation.CreateExerciseRoute
import io.github.ikafire.stronger.navigation.EditExerciseRoute
import io.github.ikafire.stronger.navigation.ExerciseDetailRoute
import io.github.ikafire.stronger.navigation.ExercisesRoute
import io.github.ikafire.stronger.navigation.HistoryRoute
import io.github.ikafire.stronger.navigation.MeasureRoute
import io.github.ikafire.stronger.navigation.PlateCalculatorRoute
import io.github.ikafire.stronger.navigation.ProfileRoute
import io.github.ikafire.stronger.navigation.SettingsRoute
import io.github.ikafire.stronger.navigation.StrongerNavHost
import io.github.ikafire.stronger.navigation.TopLevelDestination
import io.github.ikafire.stronger.navigation.WarmUpCalculatorRoute
import io.github.ikafire.stronger.navigation.WorkoutRoute
import io.github.ikafire.stronger.onboarding.OnboardingScreen
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var csvImporter: CsvImporter
    @Inject lateinit var csvExporter: CsvExporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()

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
                    val onboardingImportLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument(),
                    ) { uri ->
                        uri ?: return@rememberLauncherForActivityResult
                        val scope = kotlinx.coroutines.MainScope()
                        scope.launch {
                            try {
                                val inputStream = contentResolver.openInputStream(uri)
                                    ?: throw Exception("Cannot open file")
                                val result = csvImporter.import(inputStream)
                                Toast.makeText(
                                    this@MainActivity,
                                    "Imported ${result.workoutsImported} workouts, ${result.setsImported} sets",
                                    Toast.LENGTH_LONG,
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    when (hasCompletedOnboarding) {
                        null -> {} // Loading
                        false -> OnboardingScreen(
                            onComplete = { weightUnit, lengthUnit ->
                                viewModel.setWeightUnit(weightUnit)
                                viewModel.setLengthUnit(lengthUnit)
                                viewModel.completeOnboarding()
                            },
                            onSkip = {
                                viewModel.completeOnboarding()
                            },
                            onImportCsv = {
                                onboardingImportLauncher.launch(arrayOf("text/*", "application/octet-stream"))
                            },
                        )
                        true -> StrongerApp(
                            csvImporter = csvImporter,
                            csvExporter = csvExporter,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StrongerApp(
    csvImporter: CsvImporter,
    csvExporter: CsvExporter,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open file")
                val result = csvImporter.import(inputStream)
                Toast.makeText(
                    context,
                    "Imported ${result.workoutsImported} workouts, ${result.setsImported} sets",
                    Toast.LENGTH_LONG,
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw Exception("Cannot open file")
                csvExporter.export(outputStream)
                outputStream.close()
                // Share the file
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                Toast.makeText(context, "Export complete", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<SettingsRoute>() != true &&
            dest.hasRoute<ExerciseDetailRoute>() != true &&
            dest.hasRoute<CreateExerciseRoute>() != true &&
            dest.hasRoute<EditExerciseRoute>() != true &&
            dest.hasRoute<ActiveWorkoutRoute>() != true &&
            dest.hasRoute<AnalyticsRoute>() != true &&
            dest.hasRoute<PlateCalculatorRoute>() != true &&
            dest.hasRoute<WarmUpCalculatorRoute>() != true
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
            onImportCsv = {
                importLauncher.launch(arrayOf("text/*", "application/octet-stream"))
            },
            onExportCsv = {
                exportLauncher.launch("stronger_export.csv")
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}
