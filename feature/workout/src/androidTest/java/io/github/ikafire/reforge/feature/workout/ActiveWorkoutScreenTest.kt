package io.github.ikafire.reforge.feature.workout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.model.SetType
import io.github.ikafire.reforge.core.domain.model.Workout
import io.github.ikafire.reforge.core.domain.model.WorkoutExercise
import io.github.ikafire.reforge.core.domain.model.WorkoutSet
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.github.ikafire.reforge.core.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import org.junit.Rule
import org.junit.Test

class ActiveWorkoutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)
    private val exerciseRepository: ExerciseRepository = mockk(relaxed = true)

    private val testWorkout = Workout(
        id = "w1", name = "Test Workout",
        startedAt = Clock.System.now(), isActive = true,
    )

    private val testExercise = Exercise(
        id = "ex1", name = "Bench Press",
        category = ExerciseCategory.BARBELL,
        primaryMuscle = MuscleGroup.CHEST,
        createdAt = Clock.System.now(),
    )

    private fun setupViewModel(
        workout: Workout? = testWorkout,
        exercises: List<WorkoutExerciseWithDetails> = emptyList(),
        showDiscardDialog: Boolean = false,
        showFinishDialog: Boolean = false,
    ): WorkoutViewModel {
        coEvery { workoutRepository.getActiveWorkout() } returns MutableStateFlow(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())
        coEvery { workoutRepository.getWorkoutSets(any()) } returns flowOf(emptyList())
        return WorkoutViewModel(workoutRepository, exerciseRepository)
    }

    @Test
    fun workoutScreenDisplaysWorkoutName() {
        val vm = setupViewModel()
        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }
        composeTestRule.onNodeWithText("Test Workout").assertIsDisplayed()
    }

    @Test
    fun finishButtonIsDisplayed() {
        val vm = setupViewModel()
        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }
        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
    }

    @Test
    fun addExerciseButtonIsDisplayed() {
        val vm = setupViewModel()
        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }
        composeTestRule.onNodeWithText("Add Exercise").assertIsDisplayed()
    }

    @Test
    fun discardWorkoutButtonIsDisplayed() {
        val vm = setupViewModel()
        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }
        composeTestRule.onNodeWithText("Discard Workout").assertIsDisplayed()
    }

    @Test
    fun clickDiscardShowsConfirmationDialog() {
        val vm = setupViewModel()
        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Discard Workout").performClick()
        composeTestRule.onNodeWithText("Discard Workout?").assertIsDisplayed()
        composeTestRule.onNodeWithText("All logged data will be permanently deleted.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Discard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun clickFinishShowsFinishDialog() {
        val vm = setupViewModel()
        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Finish").performClick()
        composeTestRule.onNodeWithText("Finish Workout?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save this workout to history?").assertIsDisplayed()
    }

    @Test
    fun exerciseCardDisplaysExerciseName() {
        val set = WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0)
        val we = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        val detail = WorkoutExerciseWithDetails(
            workoutExercise = we,
            exercise = testExercise,
            sets = listOf(set),
            previousSets = emptyList(),
        )

        coEvery { workoutRepository.getActiveWorkout() } returns MutableStateFlow(testWorkout)
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(testExercise)
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(listOf(set))

        val vm = WorkoutViewModel(workoutRepository, exerciseRepository)

        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
        composeTestRule.onNodeWithText("+ Add Set").assertIsDisplayed()
    }

    @Test
    fun addWorkoutNoteButtonIsDisplayed() {
        val vm = setupViewModel()
        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("Add workout note").assertIsDisplayed()
    }

    @Test
    fun setHeadersAreDisplayed() {
        val set = WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0)
        val we = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)

        coEvery { workoutRepository.getActiveWorkout() } returns MutableStateFlow(testWorkout)
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(testExercise)
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(listOf(set))

        val vm = WorkoutViewModel(workoutRepository, exerciseRepository)

        composeTestRule.setContent {
            ActiveWorkoutScreen(
                onBackClick = {},
                allExercises = emptyList(),
                viewModel = vm,
            )
        }

        composeTestRule.onNodeWithText("SET").assertIsDisplayed()
        composeTestRule.onNodeWithText("PREVIOUS").assertIsDisplayed()
        composeTestRule.onNodeWithText("KG").assertIsDisplayed()
        composeTestRule.onNodeWithText("REPS").assertIsDisplayed()
    }
}
