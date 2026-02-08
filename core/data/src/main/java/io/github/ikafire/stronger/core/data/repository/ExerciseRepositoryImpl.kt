package io.github.ikafire.stronger.core.data.repository

import io.github.ikafire.stronger.core.data.mapper.toDomain
import io.github.ikafire.stronger.core.data.mapper.toEntity
import io.github.ikafire.stronger.core.database.dao.ExerciseDao
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.ExerciseCategory
import io.github.ikafire.stronger.core.domain.model.MuscleGroup
import io.github.ikafire.stronger.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        exerciseDao.getAllExercises().map { list -> list.map { it.toDomain() } }

    override fun getExerciseById(id: String): Flow<Exercise?> =
        exerciseDao.getExerciseById(id).map { it?.toDomain() }

    override fun searchExercises(query: String): Flow<List<Exercise>> =
        exerciseDao.searchExercises(query).map { list -> list.map { it.toDomain() } }

    override fun getExercisesByCategory(category: ExerciseCategory): Flow<List<Exercise>> =
        exerciseDao.getExercisesByCategory(category.name).map { list -> list.map { it.toDomain() } }

    override fun getExercisesByMuscle(muscle: MuscleGroup): Flow<List<Exercise>> =
        exerciseDao.getExercisesByMuscle(muscle.name).map { list -> list.map { it.toDomain() } }

    override suspend fun insertExercise(exercise: Exercise) =
        exerciseDao.insertExercise(exercise.toEntity())

    override suspend fun updateExercise(exercise: Exercise) =
        exerciseDao.updateExercise(exercise.toEntity())

    override suspend fun deleteExercise(id: String) =
        exerciseDao.deleteExercise(id)
}
