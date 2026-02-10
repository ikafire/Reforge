package io.github.ikafire.reforge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.ikafire.reforge.core.database.entity.ExerciseHistoryEntry
import io.github.ikafire.reforge.core.database.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {

    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY sortOrder ASC")
    fun getSetsForExercise(workoutExerciseId: String): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE id = :id")
    fun getSetById(id: String): Flow<WorkoutSetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity)

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSet(id: String)

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON we.id = ws.workoutExerciseId
        INNER JOIN workouts w ON w.id = we.workoutId AND w.isActive = 0
        WHERE we.exerciseId = :exerciseId
        AND ws.isCompleted = 1
        ORDER BY w.startedAt DESC, ws.sortOrder ASC
    """)
    suspend fun getPreviousSetsForExercise(exerciseId: String): List<WorkoutSetEntity>

    @Query("""
        SELECT ws.id, ws.weight, ws.reps, ws.effectiveWeight, ws.type, ws.rpe,
               w.startedAt as workoutDate, w.name as workoutName, w.id as workoutId
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON we.id = ws.workoutExerciseId
        INNER JOIN workouts w ON w.id = we.workoutId AND w.isActive = 0
        WHERE we.exerciseId = :exerciseId
        AND ws.isCompleted = 1
        ORDER BY w.startedAt DESC, ws.sortOrder ASC
    """)
    suspend fun getExerciseHistory(exerciseId: String): List<ExerciseHistoryEntry>

    @Query("UPDATE workout_sets SET weight = weight * :factor, effectiveWeight = effectiveWeight * :factor WHERE weight IS NOT NULL")
    suspend fun convertAllWeights(factor: Double)
}
