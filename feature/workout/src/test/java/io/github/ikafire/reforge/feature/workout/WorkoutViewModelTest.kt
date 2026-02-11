package io.github.ikafire.reforge.feature.workout

import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.model.ResistanceProfile
import io.github.ikafire.reforge.core.domain.model.ResistanceProfileType
import io.github.ikafire.reforge.core.domain.model.SetType
import io.github.ikafire.reforge.core.domain.model.Workout
import io.github.ikafire.reforge.core.domain.model.WorkoutExercise
import io.github.ikafire.reforge.core.domain.model.WorkoutSet
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.github.ikafire.reforge.core.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)
    private val exerciseRepository: ExerciseRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = WorkoutViewModel(workoutRepository, exerciseRepository)

    private fun makeExercise(id: String = "ex-1", name: String = "Bench Press") = Exercise(
        id = id,
        name = name,
        category = ExerciseCategory.BARBELL,
        primaryMuscle = MuscleGroup.CHEST,
        createdAt = Clock.System.now(),
    )

    // Spec: Start empty workout creates active session
    @Test
    fun `startEmptyWorkout calls repository with active workout`() = runTest {
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(null)
        val vm = createViewModel()

        var callbackInvoked = false
        vm.startEmptyWorkout { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
        val slot = slot<Workout>()
        coVerify { workoutRepository.startWorkout(capture(slot)) }
        assertTrue(slot.captured.isActive)
        assertNotNull(slot.captured.startedAt)
    }

    // Spec: Single active workout enforcement
    @Test
    fun `startEmptyWorkout with existing active workout does not create second`() = runTest {
        val existing = Workout(
            id = "w-existing", startedAt = Clock.System.now(), isActive = true,
        )
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(existing)
        val vm = createViewModel()

        var callbackInvoked = false
        vm.startEmptyWorkout { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
        coVerify(exactly = 0) { workoutRepository.startWorkout(any()) }
    }

    // Spec: Add exercises opens picker then adds with one empty set
    @Test
    fun `addExercises creates workout exercises with one empty set each`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())
        coEvery { workoutRepository.getWorkoutSets(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        val exercises = listOf(makeExercise(id = "ex1"), makeExercise(id = "ex2"))
        vm.addExercises(exercises)
        advanceUntilIdle()

        coVerify(exactly = 2) { workoutRepository.addExerciseToWorkout(any()) }
        coVerify(exactly = 2) { workoutRepository.insertSet(any()) }

        // Verify sets are WORKING type
        val setSlots = mutableListOf<WorkoutSet>()
        coVerify { workoutRepository.insertSet(capture(setSlots)) }
        setSlots.forEach { assertEquals(SetType.WORKING, it.type) }
    }

    // Spec: Add set pre-fills weight from last set
    @Test
    fun `addSet pre-fills weight from last set`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(
            listOf(
                WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0, weight = 80.0),
            )
        )
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.addSet("we1")
        advanceUntilIdle()

        val slot = slot<WorkoutSet>()
        coVerify { workoutRepository.insertSet(capture(slot)) }
        assertEquals(80.0, slot.captured.weight)
        assertEquals(1, slot.captured.sortOrder)
    }

    // Spec: Complete set calculates effective weight with resistance multiplier
    @Test
    fun `completeSet calculates effectiveWeight using resistance multiplier`() = runTest {
        val exercise = makeExercise(id = "ex1").copy(
            resistanceProfile = ResistanceProfile(
                type = ResistanceProfileType.LEVER,
                multiplier = 0.8,
            )
        )
        val set = WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0, weight = 100.0)
        val weDetail = WorkoutExerciseWithDetails(
            workoutExercise = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0),
            exercise = exercise,
            sets = listOf(set),
            previousSets = emptyList(),
        )

        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(
            listOf(WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0))
        )
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(exercise)
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(listOf(set))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.completeSet(set)
        advanceUntilIdle()

        val slot = slot<WorkoutSet>()
        coVerify { workoutRepository.updateSet(capture(slot)) }
        assertTrue(slot.captured.isCompleted)
        assertEquals(80.0, slot.captured.effectiveWeight!!, 0.001) // 100 * 0.8
    }

    // Spec: Uncomplete set clears effective weight and timestamp
    @Test
    fun `uncompleteSet clears completion fields`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        val set = WorkoutSet(
            id = "s1", workoutExerciseId = "we1", sortOrder = 0,
            isCompleted = true, completedAt = Clock.System.now(), effectiveWeight = 80.0,
        )

        vm.uncompleteSet(set)
        advanceUntilIdle()

        val slot = slot<WorkoutSet>()
        coVerify { workoutRepository.updateSet(capture(slot)) }
        assertFalse(slot.captured.isCompleted)
        assertNull(slot.captured.completedAt)
        assertNull(slot.captured.effectiveWeight)
    }

    // Spec: Set type tagging
    @Test
    fun `setSetType updates set type`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        val set = WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0)

        vm.setSetType(set, SetType.WARMUP)
        advanceUntilIdle()

        val slot = slot<WorkoutSet>()
        coVerify { workoutRepository.updateSet(capture(slot)) }
        assertEquals(SetType.WARMUP, slot.captured.type)
    }

    // Spec: Finish workout with discardIncomplete deletes incomplete sets
    @Test
    fun `finishWorkout with discardIncomplete removes incomplete sets`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)

        val completedSet = WorkoutSet(id = "s1", workoutExerciseId = "we1", sortOrder = 0, isCompleted = true)
        val incompleteSet = WorkoutSet(id = "s2", workoutExerciseId = "we1", sortOrder = 1, isCompleted = false)
        val we = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)

        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(makeExercise(id = "ex1"))
        coEvery { workoutRepository.getWorkoutSets("we1") } returns flowOf(listOf(completedSet, incompleteSet))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.finishWorkout(discardIncomplete = true)
        advanceUntilIdle()

        coVerify { workoutRepository.deleteSet("s2") }
        coVerify(exactly = 0) { workoutRepository.deleteSet("s1") }
        coVerify { workoutRepository.finishWorkout("w1") }
    }

    // Spec: Discard workout deletes all data
    @Test
    fun `discardWorkout calls repository discard`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.discardWorkout()
        advanceUntilIdle()

        coVerify { workoutRepository.discardWorkout("w1") }
    }

    // Spec: Dialog state management
    @Test
    fun `showDiscardDialog and hideDiscardDialog toggle state`() = runTest {
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(null)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.showDiscardDialog()
        assertTrue(vm.uiState.value.showDiscardDialog)

        vm.hideDiscardDialog()
        assertFalse(vm.uiState.value.showDiscardDialog)
    }

    // Spec: Reorder exercises persists sort order
    @Test
    fun `reorderExercise updates sort orders`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        val we1 = WorkoutExercise(id = "we1", workoutId = "w1", exerciseId = "ex1", sortOrder = 0)
        val we2 = WorkoutExercise(id = "we2", workoutId = "w1", exerciseId = "ex2", sortOrder = 1)

        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises("w1") } returns flowOf(listOf(we1, we2))
        coEvery { exerciseRepository.getExerciseById("ex1") } returns flowOf(makeExercise(id = "ex1"))
        coEvery { exerciseRepository.getExerciseById("ex2") } returns flowOf(makeExercise(id = "ex2"))
        coEvery { workoutRepository.getWorkoutSets(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.reorderExercise(0, 1)
        advanceUntilIdle()

        // After swap, both exercises get updated sort orders
        coVerify(atLeast = 2) { workoutRepository.updateWorkoutExercise(any()) }
    }

    // Spec: Remove exercise
    @Test
    fun `removeExercise calls repository`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        vm.removeExercise("we1")
        advanceUntilIdle()

        coVerify { workoutRepository.removeExerciseFromWorkout("we1") }
    }

    // Spec: Workout-level notes persist
    @Test
    fun `updateWorkoutNotes calls repository with updated notes`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateWorkoutNotes("Felt strong today")
        advanceUntilIdle()

        val slot = slot<Workout>()
        coVerify { workoutRepository.updateWorkout(capture(slot)) }
        assertEquals("Felt strong today", slot.captured.notes)
    }

    // Spec: Blank notes are stored as null
    @Test
    fun `updateWorkoutNotes with blank text stores null`() = runTest {
        val workout = Workout(id = "w1", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(workout)
        coEvery { workoutRepository.getWorkoutExercises(any()) } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateWorkoutNotes("   ")
        advanceUntilIdle()

        val slot = slot<Workout>()
        coVerify { workoutRepository.updateWorkout(capture(slot)) }
        assertNull(slot.captured.notes)
    }
}
