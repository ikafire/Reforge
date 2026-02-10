package io.github.ikafire.stronger.core.data.mapper

import io.github.ikafire.stronger.core.database.entity.WorkoutEntity
import io.github.ikafire.stronger.core.database.entity.WorkoutExerciseEntity
import io.github.ikafire.stronger.core.database.entity.WorkoutSetEntity
import io.github.ikafire.stronger.core.domain.model.SetType
import io.github.ikafire.stronger.core.domain.model.Workout
import io.github.ikafire.stronger.core.domain.model.WorkoutExercise
import io.github.ikafire.stronger.core.domain.model.WorkoutSet
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class WorkoutMapperTest {

    // 9.1 WorkoutEntity → Workout
    @Test
    fun `WorkoutEntity maps to Workout domain model correctly`() {
        val entity = WorkoutEntity(
            id = "w1",
            templateId = "t1",
            name = "Morning Workout",
            startedAt = 1705312800000, // 2024-01-15 10:00:00 UTC
            finishedAt = 1705316400000,
            notes = "Good session",
            isActive = false,
        )

        val workout = entity.toDomain()

        assertEquals("w1", workout.id)
        assertEquals("t1", workout.templateId)
        assertEquals("Morning Workout", workout.name)
        assertEquals(Instant.fromEpochMilliseconds(1705312800000), workout.startedAt)
        assertEquals(Instant.fromEpochMilliseconds(1705316400000), workout.finishedAt)
        assertEquals("Good session", workout.notes)
        assertEquals(false, workout.isActive)
    }

    // 9.2 Workout → WorkoutEntity maps Instant to epoch millis
    @Test
    fun `Workout maps to WorkoutEntity with epoch millis`() {
        val workout = Workout(
            id = "w1",
            templateId = null,
            name = "Session",
            startedAt = Instant.fromEpochMilliseconds(1705312800000),
            finishedAt = null,
            notes = null,
            isActive = true,
        )

        val entity = workout.toEntity()

        assertEquals("w1", entity.id)
        assertNull(entity.templateId)
        assertEquals(1705312800000, entity.startedAt)
        assertNull(entity.finishedAt)
        assertNull(entity.notes)
        assertEquals(true, entity.isActive)
    }

    // 9.3 WorkoutExerciseEntity ↔ WorkoutExercise maps restTimerSeconds
    @Test
    fun `WorkoutExerciseEntity maps restTimerSeconds`() {
        val entity = WorkoutExerciseEntity(
            id = "we1",
            workoutId = "w1",
            exerciseId = "e1",
            sortOrder = 0,
            supersetGroup = null,
            notes = "Heavy",
            restTimerSeconds = 120,
        )

        val domain = entity.toDomain()
        assertEquals(120, domain.restTimerSeconds)
        assertEquals("Heavy", domain.notes)

        val backToEntity = domain.toEntity()
        assertEquals(120, backToEntity.restTimerSeconds)
    }

    @Test
    fun `WorkoutExercise with null restTimerSeconds maps correctly`() {
        val domain = WorkoutExercise(
            id = "we1",
            workoutId = "w1",
            exerciseId = "e1",
            sortOrder = 0,
            restTimerSeconds = null,
        )
        assertNull(domain.toEntity().restTimerSeconds)
    }

    // 9.4 WorkoutSetEntity → WorkoutSet maps SetType string to enum
    @Test
    fun `WorkoutSetEntity maps SetType correctly`() {
        for (setType in SetType.entries) {
            val entity = WorkoutSetEntity(
                id = "s1",
                workoutExerciseId = "we1",
                sortOrder = 0,
                type = setType.name,
            )
            assertEquals(setType, entity.toDomain().type)
        }
    }

    // 9.5 WorkoutSet → WorkoutSetEntity maps Duration to millis
    @Test
    fun `WorkoutSet maps Duration to millis`() {
        val set = WorkoutSet(
            id = "s1",
            workoutExerciseId = "we1",
            sortOrder = 0,
            type = SetType.WORKING,
            duration = 90.seconds,
        )

        val entity = set.toEntity()
        assertEquals(90_000L, entity.durationMs)
    }

    @Test
    fun `WorkoutSetEntity with null durationMs maps to null Duration`() {
        val entity = WorkoutSetEntity(
            id = "s1",
            workoutExerciseId = "we1",
            sortOrder = 0,
            durationMs = null,
        )
        assertNull(entity.toDomain().duration)
    }

    @Test
    fun `WorkoutSetEntity maps completedAt millis to Instant`() {
        val entity = WorkoutSetEntity(
            id = "s1",
            workoutExerciseId = "we1",
            sortOrder = 0,
            completedAt = 1705312800000,
        )
        assertEquals(Instant.fromEpochMilliseconds(1705312800000), entity.toDomain().completedAt)
    }
}
