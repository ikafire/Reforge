package io.github.ikafire.stronger.core.data.repository

import io.github.ikafire.stronger.core.data.mapper.toDomain
import io.github.ikafire.stronger.core.data.mapper.toEntity
import io.github.ikafire.stronger.core.database.dao.WorkoutDao
import io.github.ikafire.stronger.core.database.dao.WorkoutExerciseDao
import io.github.ikafire.stronger.core.database.dao.WorkoutSetDao
import io.github.ikafire.stronger.core.domain.model.Workout
import io.github.ikafire.stronger.core.domain.model.WorkoutExercise
import io.github.ikafire.stronger.core.domain.model.WorkoutSet
import io.github.ikafire.stronger.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutSetDao: WorkoutSetDao,
) : WorkoutRepository {

    override fun getActiveWorkout(): Flow<Workout?> =
        workoutDao.getActiveWorkout().map { it?.toDomain() }

    override fun getWorkoutHistory(): Flow<List<Workout>> =
        workoutDao.getWorkoutHistory().map { list -> list.map { it.toDomain() } }

    override fun getWorkoutById(id: String): Flow<Workout?> =
        workoutDao.getWorkoutById(id).map { it?.toDomain() }

    override fun getWorkoutExercises(workoutId: String): Flow<List<WorkoutExercise>> =
        workoutExerciseDao.getWorkoutExercises(workoutId).map { list -> list.map { it.toDomain() } }

    override fun getWorkoutSets(workoutExerciseId: String): Flow<List<WorkoutSet>> =
        workoutSetDao.getSetsForExercise(workoutExerciseId).map { list -> list.map { it.toDomain() } }

    override suspend fun startWorkout(workout: Workout) =
        workoutDao.insertWorkout(workout.toEntity())

    override suspend fun finishWorkout(workoutId: String) {
        val entity = workoutDao.getWorkoutById(workoutId)
        // Update handled via the update path
    }

    override suspend fun discardWorkout(workoutId: String) =
        workoutDao.deleteWorkout(workoutId)

    override suspend fun addExerciseToWorkout(workoutExercise: WorkoutExercise) =
        workoutExerciseDao.insertWorkoutExercise(workoutExercise.toEntity())

    override suspend fun removeExerciseFromWorkout(workoutExerciseId: String) =
        workoutExerciseDao.deleteWorkoutExercise(workoutExerciseId)

    override suspend fun updateWorkout(workout: Workout) =
        workoutDao.updateWorkout(workout.toEntity())

    override suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise) =
        workoutExerciseDao.updateWorkoutExercise(workoutExercise.toEntity())

    override suspend fun insertSet(set: WorkoutSet) =
        workoutSetDao.insertSet(set.toEntity())

    override suspend fun updateSet(set: WorkoutSet) =
        workoutSetDao.updateSet(set.toEntity())

    override suspend fun deleteSet(setId: String) =
        workoutSetDao.deleteSet(setId)

    override suspend fun deleteWorkout(workoutId: String) =
        workoutDao.deleteWorkout(workoutId)
}
