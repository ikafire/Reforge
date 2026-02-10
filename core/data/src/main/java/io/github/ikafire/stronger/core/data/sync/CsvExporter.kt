package io.github.ikafire.stronger.core.data.sync

import io.github.ikafire.stronger.core.domain.model.SetType
import io.github.ikafire.stronger.core.domain.repository.ExerciseRepository
import io.github.ikafire.stronger.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.OutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject

class CsvExporter @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) {
    suspend fun export(outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        writer.write("\"Date\";\"Workout Name\";\"Exercise Name\";\"Set Order\";\"Weight\";\"Reps\";\"Distance\";\"Seconds\";\"Notes\";\"Workout Notes\";\"RPE\"\n")

        val workouts = workoutRepository.getWorkoutHistory().first()
        for (workout in workouts) {
            val date = workout.startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
            val dateStr = "%04d-%02d-%02d %02d:%02d:%02d".format(
                date.year, date.monthNumber, date.dayOfMonth,
                date.hour, date.minute, date.second,
            )
            val workoutName = workout.name ?: "Workout"

            val exercises = workoutRepository.getWorkoutExercises(workout.id).first()
            for (we in exercises) {
                val exercise = exerciseRepository.getExerciseById(we.exerciseId).first()
                val exerciseName = exercise?.name ?: "Unknown"
                val sets = workoutRepository.getWorkoutSets(we.id).first()

                for (set in sets.filter { it.isCompleted }) {
                    val setOrder = when (set.type) {
                        SetType.WARMUP -> "W"
                        SetType.DROP -> "D"
                        SetType.FAILURE -> "F"
                        SetType.WORKING -> set.sortOrder.plus(1).toString()
                    }
                    val weight = set.weight?.let { "%.1f".format(it) } ?: ""
                    val reps = set.reps?.toString() ?: ""
                    val distance = set.distance?.let { "%.2f".format(it) } ?: ""
                    val seconds = set.duration?.inWholeSeconds?.toString() ?: ""
                    val notes = we.notes ?: ""
                    val workoutNotes = workout.notes ?: ""
                    val rpe = set.rpe?.let { "%.1f".format(it) } ?: ""

                    writer.write("\"$dateStr\";\"$workoutName\";\"$exerciseName\";\"$setOrder\";\"$weight\";\"$reps\";\"$distance\";\"$seconds\";\"$notes\";\"$workoutNotes\";\"$rpe\"\n")
                }
            }
        }
        writer.flush()
    }
}
