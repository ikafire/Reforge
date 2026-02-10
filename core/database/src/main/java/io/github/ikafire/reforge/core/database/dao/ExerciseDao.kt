package io.github.ikafire.reforge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.ikafire.reforge.core.database.entity.ExerciseEntity
import io.github.ikafire.reforge.core.database.entity.ExerciseWithUsageCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun getExerciseById(id: String): Flow<ExerciseEntity?>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY name ASC")
    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE primaryMuscle = :muscle ORDER BY name ASC")
    fun getExercisesByMuscle(muscle: String): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: String)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("""
        SELECT e.*, COALESCE(usage.count, 0) as usageCount
        FROM exercises e
        LEFT JOIN (
            SELECT we.exerciseId, COUNT(DISTINCT we.workoutId) as count
            FROM workout_exercises we
            INNER JOIN workouts w ON w.id = we.workoutId AND w.isActive = 0
            GROUP BY we.exerciseId
        ) usage ON usage.exerciseId = e.id
        ORDER BY usageCount DESC, e.name ASC
    """)
    fun getAllExercisesWithUsageCount(): Flow<List<ExerciseWithUsageCount>>
}
