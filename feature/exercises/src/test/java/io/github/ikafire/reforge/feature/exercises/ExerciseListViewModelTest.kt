package io.github.ikafire.reforge.feature.exercises

import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.ExerciseWithUsage
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val exerciseRepository: ExerciseRepository = mockk(relaxed = true)

    private fun makeExercise(
        id: String,
        name: String,
        category: ExerciseCategory = ExerciseCategory.BARBELL,
        primaryMuscle: MuscleGroup = MuscleGroup.CHEST,
        secondaryMuscles: List<MuscleGroup> = emptyList(),
    ) = Exercise(
        id = id,
        name = name,
        category = category,
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        createdAt = Clock.System.now(),
    )

    private fun withUsage(exercise: Exercise, count: Int) =
        ExerciseWithUsage(exercise = exercise, usageCount = count)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exercises grouped by usage frequency`() = runTest {
        val exercises = listOf(
            withUsage(makeExercise("1", "Bench Press"), 55),
            withUsage(makeExercise("2", "Squat"), 30),
            withUsage(makeExercise("3", "Deadlift"), 15),
            withUsage(makeExercise("4", "Curl"), 5),
            withUsage(makeExercise("5", "Lateral Raise"), 0),
        )
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)

        val vm = ExerciseListViewModel(exerciseRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(5, state.groupedExercises.size)
        assertEquals(1, state.groupedExercises[UsageGroup.HEAVY]?.size)
        assertEquals(1, state.groupedExercises[UsageGroup.FREQUENT]?.size)
        assertEquals(1, state.groupedExercises[UsageGroup.MODERATE]?.size)
        assertEquals(1, state.groupedExercises[UsageGroup.LIGHT]?.size)
        assertEquals(1, state.groupedExercises[UsageGroup.NEVER]?.size)
    }

    @Test
    fun `search filters by name case insensitive`() = runTest {
        val exercises = listOf(
            withUsage(makeExercise("1", "Bench Press"), 10),
            withUsage(makeExercise("2", "Incline Bench"), 5),
            withUsage(makeExercise("3", "Squat"), 20),
        )
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)

        val vm = ExerciseListViewModel(exerciseRepository)
        advanceUntilIdle()

        vm.onSearchQueryChange("bench")
        advanceUntilIdle()

        val allExercises = vm.uiState.value.groupedExercises.values.flatten()
        assertEquals(2, allExercises.size)
        assertTrue(allExercises.all { "bench" in it.exercise.name.lowercase() })
    }

    @Test
    fun `category filter returns only matching category`() = runTest {
        val exercises = listOf(
            withUsage(makeExercise("1", "Bench Press", category = ExerciseCategory.BARBELL), 10),
            withUsage(makeExercise("2", "DB Curl", category = ExerciseCategory.DUMBBELL), 5),
            withUsage(makeExercise("3", "Cable Fly", category = ExerciseCategory.CABLE), 5),
        )
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)

        val vm = ExerciseListViewModel(exerciseRepository)
        advanceUntilIdle()

        vm.onCategorySelected(ExerciseCategory.DUMBBELL)
        advanceUntilIdle()

        val allExercises = vm.uiState.value.groupedExercises.values.flatten()
        assertEquals(1, allExercises.size)
        assertEquals("DB Curl", allExercises[0].exercise.name)
    }

    @Test
    fun `muscle filter matches primary and secondary muscles`() = runTest {
        val exercises = listOf(
            withUsage(makeExercise("1", "Bench Press", primaryMuscle = MuscleGroup.CHEST, secondaryMuscles = listOf(MuscleGroup.TRICEPS)), 10),
            withUsage(makeExercise("2", "Tricep Extension", primaryMuscle = MuscleGroup.TRICEPS), 5),
            withUsage(makeExercise("3", "Squat", primaryMuscle = MuscleGroup.QUADS), 20),
        )
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)

        val vm = ExerciseListViewModel(exerciseRepository)
        advanceUntilIdle()

        vm.onMuscleSelected(MuscleGroup.TRICEPS)
        advanceUntilIdle()

        val allExercises = vm.uiState.value.groupedExercises.values.flatten()
        assertEquals(2, allExercises.size) // Bench (secondary) + Tricep Extension (primary)
    }

    @Test
    fun `combined filters narrow results`() = runTest {
        val exercises = listOf(
            withUsage(makeExercise("1", "Barbell Bench Press", category = ExerciseCategory.BARBELL, primaryMuscle = MuscleGroup.CHEST), 10),
            withUsage(makeExercise("2", "Dumbbell Bench Press", category = ExerciseCategory.DUMBBELL, primaryMuscle = MuscleGroup.CHEST), 5),
            withUsage(makeExercise("3", "Barbell Squat", category = ExerciseCategory.BARBELL, primaryMuscle = MuscleGroup.QUADS), 20),
        )
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)

        val vm = ExerciseListViewModel(exerciseRepository)
        advanceUntilIdle()

        vm.onSearchQueryChange("bench")
        vm.onCategorySelected(ExerciseCategory.BARBELL)
        advanceUntilIdle()

        val allExercises = vm.uiState.value.groupedExercises.values.flatten()
        assertEquals(1, allExercises.size)
        assertEquals("Barbell Bench Press", allExercises[0].exercise.name)
    }

    @Test
    fun `clearing category filter shows all exercises`() = runTest {
        val exercises = listOf(
            withUsage(makeExercise("1", "Bench Press", category = ExerciseCategory.BARBELL), 10),
            withUsage(makeExercise("2", "DB Curl", category = ExerciseCategory.DUMBBELL), 5),
        )
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)

        val vm = ExerciseListViewModel(exerciseRepository)
        advanceUntilIdle()

        vm.onCategorySelected(ExerciseCategory.BARBELL)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.groupedExercises.values.flatten().size)

        vm.onCategorySelected(null)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.groupedExercises.values.flatten().size)
    }

    @Test
    fun `empty groups are excluded from result`() = runTest {
        val exercises = listOf(
            withUsage(makeExercise("1", "Bench Press"), 5), // LIGHT only
        )
        every { exerciseRepository.getAllExercisesWithUsageCount() } returns flowOf(exercises)

        val vm = ExerciseListViewModel(exerciseRepository)
        advanceUntilIdle()

        val groups = vm.uiState.value.groupedExercises
        assertEquals(1, groups.size)
        assertTrue(groups.containsKey(UsageGroup.LIGHT))
    }
}
