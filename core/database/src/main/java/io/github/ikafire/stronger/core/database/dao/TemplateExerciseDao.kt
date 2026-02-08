package io.github.ikafire.stronger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.ikafire.stronger.core.database.entity.TemplateExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateExerciseDao {

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY sortOrder ASC")
    fun getTemplateExercises(templateId: String): Flow<List<TemplateExerciseEntity>>

    @Query("SELECT * FROM template_exercises WHERE id = :id")
    fun getTemplateExerciseById(id: String): Flow<TemplateExerciseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercise(exercise: TemplateExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(exercises: List<TemplateExerciseEntity>)

    @Update
    suspend fun updateTemplateExercise(exercise: TemplateExerciseEntity)

    @Query("DELETE FROM template_exercises WHERE id = :id")
    suspend fun deleteTemplateExercise(id: String)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteTemplateExercisesByTemplate(templateId: String)
}
