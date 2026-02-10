package io.github.ikafire.stronger.core.domain.repository

import io.github.ikafire.stronger.core.domain.model.ExerciseHistoryItem
import io.github.ikafire.stronger.core.domain.model.Workout
import io.github.ikafire.stronger.core.domain.model.WorkoutExercise
import io.github.ikafire.stronger.core.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getActiveWorkout(): Flow<Workout?>
    fun getWorkoutHistory(): Flow<List<Workout>>
    fun getWorkoutById(id: String): Flow<Workout?>
    fun getWorkoutExercises(workoutId: String): Flow<List<WorkoutExercise>>
    fun getWorkoutSets(workoutExerciseId: String): Flow<List<WorkoutSet>>
    suspend fun startWorkout(workout: Workout)
    suspend fun finishWorkout(workoutId: String)
    suspend fun discardWorkout(workoutId: String)
    suspend fun addExerciseToWorkout(workoutExercise: WorkoutExercise)
    suspend fun removeExerciseFromWorkout(workoutExerciseId: String)
    suspend fun updateWorkout(workout: Workout)
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise)
    suspend fun insertSet(set: WorkoutSet)
    suspend fun updateSet(set: WorkoutSet)
    suspend fun deleteSet(setId: String)
    suspend fun deleteWorkout(workoutId: String)
    suspend fun getCompletedSetsForExercise(exerciseId: String): List<WorkoutSet>
    suspend fun getExerciseHistory(exerciseId: String): List<ExerciseHistoryItem>
    suspend fun convertAllWeights(factor: Double)
}
