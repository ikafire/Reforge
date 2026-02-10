package io.github.ikafire.reforge.core.data.sync

import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.model.Workout
import io.github.ikafire.reforge.core.domain.model.WorkoutExercise
import io.github.ikafire.reforge.core.domain.model.WorkoutSet
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.github.ikafire.reforge.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

data class ImportResult(
    val workoutsImported: Int,
    val exercisesImported: Int,
    val setsImported: Int,
    val exercisesCreated: Int,
)

class CsvImporter @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) {
    private val parser = StrongCsvParser()

    suspend fun import(
        inputStream: InputStream,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
    ): ImportResult {
        val csvWorkouts = parser.parse(inputStream)
        val allExercises = exerciseRepository.getAllExercises().first()
        val exerciseNameMap = allExercises.associateBy { it.name.lowercase() }
        var exercisesCreated = 0
        var totalSets = 0
        val resolvedExercises = mutableMapOf<String, Exercise>()

        csvWorkouts.forEachIndexed { index, csvWorkout ->
            onProgress(index + 1, csvWorkouts.size)

            // Parse date: Strong format is "YYYY-MM-DD HH:MM:SS"
            val startedAt = try {
                val parts = csvWorkout.date.split(" ")
                val dateParts = parts[0].split("-")
                val timeParts = parts.getOrElse(1) { "00:00:00" }.split(":")
                LocalDateTime(
                    dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt(),
                    timeParts[0].toInt(), timeParts[1].toInt(), timeParts.getOrElse(2) { "0" }.toInt(),
                ).toInstant(TimeZone.currentSystemDefault())
            } catch (_: Exception) {
                kotlinx.datetime.Clock.System.now()
            }

            val finishedAt = csvWorkout.durationSeconds?.let {
                startedAt.plus(kotlin.time.Duration.parse("${it}s"))
            }

            val workoutId = UUID.randomUUID().toString()
            val workout = Workout(
                id = workoutId,
                name = csvWorkout.workoutName.ifBlank { null },
                startedAt = startedAt,
                finishedAt = finishedAt,
                isActive = false,
            )
            workoutRepository.startWorkout(workout)
            if (finishedAt != null) {
                workoutRepository.updateWorkout(workout.copy(finishedAt = finishedAt))
            }

            csvWorkout.exercises.forEachIndexed { exIdx, csvExercise ->
                // Match exercise name to library
                val exercise = resolvedExercises.getOrPut(csvExercise.name.lowercase()) {
                    matchExercise(csvExercise.name, exerciseNameMap) ?: run {
                        // Create custom exercise
                        val newExercise = Exercise(
                            id = UUID.randomUUID().toString(),
                            name = csvExercise.name,
                            category = ExerciseCategory.OTHER,
                            primaryMuscle = MuscleGroup.OTHER,
                            isCustom = true,
                            createdAt = kotlinx.datetime.Clock.System.now(),
                        )
                        exerciseRepository.insertExercise(newExercise)
                        exercisesCreated++
                        newExercise
                    }
                }

                val weId = UUID.randomUUID().toString()
                val workoutExercise = WorkoutExercise(
                    id = weId,
                    workoutId = workoutId,
                    exerciseId = exercise.id,
                    sortOrder = exIdx,
                    notes = csvExercise.notes,
                )
                workoutRepository.addExerciseToWorkout(workoutExercise)

                csvExercise.sets.forEachIndexed { setIdx, csvSet ->
                    val set = WorkoutSet(
                        id = UUID.randomUUID().toString(),
                        workoutExerciseId = weId,
                        sortOrder = setIdx,
                        type = csvSet.type,
                        weight = csvSet.weight,
                        reps = csvSet.reps,
                        distance = csvSet.distance,
                        rpe = csvSet.rpe,
                        isCompleted = true,
                        completedAt = startedAt,
                        effectiveWeight = csvSet.weight,
                    )
                    workoutRepository.insertSet(set)
                    totalSets++
                }
            }
        }

        return ImportResult(
            workoutsImported = csvWorkouts.size,
            exercisesImported = csvWorkouts.sumOf { it.exercises.size },
            setsImported = totalSets,
            exercisesCreated = exercisesCreated,
        )
    }

    internal fun matchExercise(name: String, library: Map<String, Exercise>): Exercise? {
        // Exact match (case-insensitive)
        library[name.lowercase()]?.let { return it }

        // Normalized match: remove parenthetical equipment labels
        val normalized = name.replace(Regex("\\s*\\([^)]*\\)"), "").trim().lowercase()
        library[normalized]?.let { return it }

        // Fuzzy match: find best match by substring containment
        val bestMatch = library.entries.firstOrNull { (key, _) ->
            key.contains(normalized) || normalized.contains(key)
        }
        return bestMatch?.value
    }
}
