package io.github.ikafire.stronger.core.domain.repository

import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.ExerciseCategory
import io.github.ikafire.stronger.core.domain.model.ExerciseWithUsage
import io.github.ikafire.stronger.core.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getAllExercisesWithUsageCount(): Flow<List<ExerciseWithUsage>>
    fun getExerciseById(id: String): Flow<Exercise?>
    fun searchExercises(query: String): Flow<List<Exercise>>
    fun getExercisesByCategory(category: ExerciseCategory): Flow<List<Exercise>>
    fun getExercisesByMuscle(muscle: MuscleGroup): Flow<List<Exercise>>
    suspend fun insertExercise(exercise: Exercise)
    suspend fun insertExercises(exercises: List<Exercise>)
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(id: String)
    suspend fun getExerciseCount(): Int
}
