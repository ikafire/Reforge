package io.github.ikafire.reforge.feature.workout

import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.model.TemplateExercise
import io.github.ikafire.reforge.core.domain.model.Workout
import io.github.ikafire.reforge.core.domain.model.WorkoutExercise
import io.github.ikafire.reforge.core.domain.model.WorkoutSet
import io.github.ikafire.reforge.core.domain.model.WorkoutTemplate
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.github.ikafire.reforge.core.domain.repository.TemplateRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val templateRepository: TemplateRepository = mockk(relaxed = true)
    private val exerciseRepository: ExerciseRepository = mockk(relaxed = true)
    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { templateRepository.getAllTemplates() } returns flowOf(emptyList())
        coEvery { templateRepository.getAllFolders() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = TemplateViewModel(templateRepository, exerciseRepository, workoutRepository)

    // Spec: Create unlimited templates
    @Test
    fun `createTemplate inserts template with given name`() = runTest {
        val vm = createViewModel()
        vm.createTemplate("Push Day")
        advanceUntilIdle()

        val slot = slot<WorkoutTemplate>()
        coVerify { templateRepository.insertTemplate(capture(slot)) }
        assertEquals("Push Day", slot.captured.name)
    }

    // Spec: Template folders
    @Test
    fun `createFolder inserts folder`() = runTest {
        val vm = createViewModel()
        vm.createFolder("PPL")
        advanceUntilIdle()

        coVerify { templateRepository.insertFolder(any()) }
    }

    // Spec: Delete template
    @Test
    fun `deleteTemplate calls repository`() = runTest {
        val vm = createViewModel()
        vm.deleteTemplate("t1")
        advanceUntilIdle()

        coVerify { templateRepository.deleteTemplate("t1") }
    }

    // Spec: Duplicate template
    @Test
    fun `duplicateTemplate calls repository`() = runTest {
        val vm = createViewModel()
        vm.duplicateTemplate("t1")
        advanceUntilIdle()

        coVerify { templateRepository.duplicateTemplate("t1") }
    }

    // Spec: Move template between folders
    @Test
    fun `moveTemplate updates template folderId`() = runTest {
        val template = WorkoutTemplate(id = "t1", name = "Push", folderId = null)
        coEvery { templateRepository.getTemplateById("t1") } returns flowOf(template)

        val vm = createViewModel()
        vm.moveTemplate("t1", "f1")
        advanceUntilIdle()

        val slot = slot<WorkoutTemplate>()
        coVerify { templateRepository.updateTemplate(capture(slot)) }
        assertEquals("f1", slot.captured.folderId)
    }

    // Spec: Add exercise to template
    @Test
    fun `addExerciseToTemplate creates template exercise with correct sort order`() = runTest {
        coEvery { templateRepository.getTemplateExercises("t1") } returns flowOf(
            listOf(
                TemplateExercise(id = "te1", templateId = "t1", exerciseId = "ex1", sortOrder = 0),
            )
        )

        val vm = createViewModel()
        vm.addExerciseToTemplate("t1", "ex2")
        advanceUntilIdle()

        val slot = slot<TemplateExercise>()
        coVerify { templateRepository.insertTemplateExercise(capture(slot)) }
        assertEquals("t1", slot.captured.templateId)
        assertEquals("ex2", slot.captured.exerciseId)
        assertEquals(1, slot.captured.sortOrder) // After existing exercise
    }

    // Spec: Start from template pre-populates exercises with target sets
    @Test
    fun `startWorkoutFromTemplate creates workout with exercises and target sets`() = runTest {
        val template = WorkoutTemplate(id = "t1", name = "Push Day")
        val templateExercises = listOf(
            TemplateExercise(id = "te1", templateId = "t1", exerciseId = "ex1", sortOrder = 0, targetSets = 4),
            TemplateExercise(id = "te2", templateId = "t1", exerciseId = "ex2", sortOrder = 1, targetSets = 3),
        )

        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(null)
        coEvery { templateRepository.getTemplateById("t1") } returns flowOf(template)
        coEvery { templateRepository.getTemplateExercises("t1") } returns flowOf(templateExercises)

        val vm = createViewModel()
        vm.startWorkoutFromTemplate("t1")
        advanceUntilIdle()

        // Workout created
        val workoutSlot = slot<Workout>()
        coVerify { workoutRepository.startWorkout(capture(workoutSlot)) }
        assertEquals("Push Day", workoutSlot.captured.name)
        assertEquals("t1", workoutSlot.captured.templateId)
        assertTrue(workoutSlot.captured.isActive)

        // 2 exercises added
        coVerify(exactly = 2) { workoutRepository.addExerciseToWorkout(any()) }

        // 4 + 3 = 7 sets created
        coVerify(exactly = 7) { workoutRepository.insertSet(any()) }
    }

    // Spec: Single active workout enforcement when starting from template
    @Test
    fun `startWorkoutFromTemplate does nothing when active workout exists`() = runTest {
        val existing = Workout(id = "w-existing", startedAt = Clock.System.now(), isActive = true)
        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(existing)

        val vm = createViewModel()
        vm.startWorkoutFromTemplate("t1")
        advanceUntilIdle()

        coVerify(exactly = 0) { workoutRepository.startWorkout(any()) }
    }

    // Spec: Superset configuration carries over from template
    @Test
    fun `startWorkoutFromTemplate preserves superset groups`() = runTest {
        val template = WorkoutTemplate(id = "t1", name = "Push Day")
        val templateExercises = listOf(
            TemplateExercise(id = "te1", templateId = "t1", exerciseId = "ex1", sortOrder = 0, targetSets = 1, supersetGroup = 1),
            TemplateExercise(id = "te2", templateId = "t1", exerciseId = "ex2", sortOrder = 1, targetSets = 1, supersetGroup = 1),
        )

        coEvery { workoutRepository.getActiveWorkout() } returns flowOf(null)
        coEvery { templateRepository.getTemplateById("t1") } returns flowOf(template)
        coEvery { templateRepository.getTemplateExercises("t1") } returns flowOf(templateExercises)

        val vm = createViewModel()
        vm.startWorkoutFromTemplate("t1")
        advanceUntilIdle()

        val weSlots = mutableListOf<WorkoutExercise>()
        coVerify(exactly = 2) { workoutRepository.addExerciseToWorkout(capture(weSlots)) }
        assertEquals(1, weSlots[0].supersetGroup)
        assertEquals(1, weSlots[1].supersetGroup)
    }
}
