package io.github.ikafire.reforge.feature.exercises

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.ExerciseWithUsage
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import org.junit.Rule
import org.junit.Test

class ExerciseListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val exerciseRepository: ExerciseRepository = mockk(relaxed = true)

    private fun makeExercise(
        id: String,
        name: String,
        category: ExerciseCategory = ExerciseCategory.BARBELL,
        primaryMuscle: MuscleGroup = MuscleGroup.CHEST,
    ) = Exercise(
        id = id, name = name, category = category,
        primaryMuscle = primaryMuscle, createdAt = Clock.System.now(),
    )

    private fun setupWithExercises(exercises: List<ExerciseWithUsage>): ExerciseListViewModel {
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)
        return ExerciseListViewModel(exerciseRepository)
    }

    @Test
    fun screenDisplaysTitleAndSearchIcon() {
        val vm = setupWithExercises(emptyList())
        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Exercises").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsNoExercisesFound() {
        val vm = setupWithExercises(emptyList())
        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("No exercises found").assertIsDisplayed()
    }

    @Test
    fun exercisesDisplayedWithNameAndMuscle() {
        val exercises = listOf(
            ExerciseWithUsage(makeExercise("1", "Bench Press", primaryMuscle = MuscleGroup.CHEST), 10),
            ExerciseWithUsage(makeExercise("2", "Barbell Row", primaryMuscle = MuscleGroup.BACK), 5),
        )
        val vm = setupWithExercises(exercises)

        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Barbell Row").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun usageGroupHeadersAreDisplayed() {
        val exercises = listOf(
            ExerciseWithUsage(makeExercise("1", "Bench Press"), 55),
            ExerciseWithUsage(makeExercise("2", "Squat"), 5),
        )
        val vm = setupWithExercises(exercises)

        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("50+ times").assertIsDisplayed()
        composeTestRule.onNodeWithText("1-10 times").assertIsDisplayed()
    }

    @Test
    fun filterChipsAreDisplayed() {
        val vm = setupWithExercises(emptyList())
        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Equipment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Muscle").assertIsDisplayed()
    }

    @Test
    fun clickingEquipmentChipShowsCategoryOptions() {
        val vm = setupWithExercises(emptyList())
        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Equipment").performClick()
        composeTestRule.onNodeWithText("Barbell").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dumbbell").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cable").assertIsDisplayed()
        composeTestRule.onNodeWithText("Machine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bodyweight").assertIsDisplayed()
    }

    @Test
    fun clickingMuscleChipShowsMuscleOptions() {
        val vm = setupWithExercises(emptyList())
        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Muscle").performClick()
        composeTestRule.onNodeWithText("Chest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shoulders").assertIsDisplayed()
    }

    @Test
    fun createExerciseFabIsDisplayed() {
        val vm = setupWithExercises(emptyList())
        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithContentDescription("Create exercise").assertIsDisplayed()
    }

    @Test
    fun clickingSearchIconShowsSearchField() {
        val vm = setupWithExercises(emptyList())
        composeTestRule.setContent {
            ExerciseListScreen(
                onExerciseClick = {},
                onCreateExerciseClick = {},
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Search exercises...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }
}
