package io.github.ikafire.reforge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.ikafire.reforge.core.database.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts WHERE isActive = 1 LIMIT 1")
    fun getActiveWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE isActive = 0 ORDER BY startedAt DESC")
    fun getWorkoutHistory(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutById(id: String): Flow<WorkoutEntity?>

    @Query("SELECT COUNT(*) FROM workouts WHERE isActive = 0")
    fun getCompletedWorkoutCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: String)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutByIdSync(id: String): WorkoutEntity?
}
