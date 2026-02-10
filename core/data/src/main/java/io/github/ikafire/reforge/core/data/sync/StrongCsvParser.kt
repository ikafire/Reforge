package io.github.ikafire.reforge.core.data.sync

import io.github.ikafire.reforge.core.domain.model.SetType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class CsvWorkout(
    val date: String,
    val workoutName: String,
    val exercises: List<CsvExercise>,
    val durationSeconds: Int? = null,
)

data class CsvExercise(
    val name: String,
    val sets: List<CsvSet>,
    val notes: String? = null,
)

data class CsvSet(
    val setOrder: Int,
    val weight: Double?,
    val reps: Int?,
    val distance: Double?,
    val durationSeconds: Int?,
    val type: SetType,
    val rpe: Double?,
)

class StrongCsvParser {

    fun parse(inputStream: InputStream): List<CsvWorkout> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        if (lines.isEmpty()) return emptyList()

        // Strong CSV format: semicolon-delimited, double-quoted values
        // Header: Date;Workout Name;Exercise Name;Set Order;Weight;Reps;Distance;Seconds;Notes;Workout Notes;RPE
        val header = parseCsvLine(lines.first())
        val dateIdx = header.indexOf("Date")
        val workoutNameIdx = header.indexOf("Workout Name")
        val exerciseNameIdx = header.indexOf("Exercise Name")
        val setOrderIdx = header.indexOf("Set Order")
        val weightIdx = header.indexOf("Weight")
        val repsIdx = header.indexOf("Reps")
        val distanceIdx = header.indexOf("Distance")
        val secondsIdx = header.indexOf("Seconds")
        val notesIdx = header.indexOf("Notes")
        val workoutNotesIdx = header.indexOf("Workout Notes")
        val rpeIdx = header.indexOf("RPE")

        data class RawRow(
            val date: String,
            val workoutName: String,
            val exerciseName: String,
            val setOrder: String,
            val weight: String,
            val reps: String,
            val distance: String,
            val seconds: String,
            val notes: String,
            val workoutNotes: String,
            val rpe: String,
        )

        val rows = lines.drop(1).map { line ->
            val cols = parseCsvLine(line)
            RawRow(
                date = cols.getOrElse(dateIdx) { "" },
                workoutName = cols.getOrElse(workoutNameIdx) { "" },
                exerciseName = cols.getOrElse(exerciseNameIdx) { "" },
                setOrder = cols.getOrElse(setOrderIdx) { "" },
                weight = cols.getOrElse(weightIdx) { "" },
                reps = cols.getOrElse(repsIdx) { "" },
                distance = cols.getOrElse(distanceIdx) { "" },
                seconds = cols.getOrElse(secondsIdx) { "" },
                notes = cols.getOrElse(notesIdx) { "" },
                workoutNotes = cols.getOrElse(workoutNotesIdx) { "" },
                rpe = cols.getOrElse(rpeIdx) { "" },
            )
        }

        // Group by date + workout name to form workouts
        val workoutGroups = rows.groupBy { "${it.date}|${it.workoutName}" }

        return workoutGroups.map { (_, workoutRows) ->
            val first = workoutRows.first()

            // Group by exercise name within workout
            val exerciseGroups = workoutRows.groupBy { it.exerciseName }

            val exercises = exerciseGroups.map { (exerciseName, exerciseRows) ->
                var exerciseNotes: String? = null
                val sets = exerciseRows.mapNotNull { row ->
                    // Handle special Set Order values
                    when (row.setOrder) {
                        "Note" -> {
                            exerciseNotes = row.notes.ifBlank { null }
                            null
                        }
                        "Rest Timer" -> null // Skip rest timer entries
                        else -> {
                            val setType = when (row.setOrder) {
                                "W" -> SetType.WARMUP
                                "D" -> SetType.DROP
                                "F" -> SetType.FAILURE
                                else -> SetType.WORKING
                            }
                            CsvSet(
                                setOrder = row.setOrder.toIntOrNull() ?: 0,
                                weight = row.weight.toDoubleOrNull(),
                                reps = row.reps.toIntOrNull(),
                                distance = row.distance.toDoubleOrNull(),
                                durationSeconds = row.seconds.toIntOrNull(),
                                type = setType,
                                rpe = row.rpe.toDoubleOrNull(),
                            )
                        }
                    }
                }

                CsvExercise(
                    name = exerciseName,
                    sets = sets,
                    notes = exerciseNotes,
                )
            }

            CsvWorkout(
                date = first.date,
                workoutName = first.workoutName,
                exercises = exercises,
            )
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ';' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
