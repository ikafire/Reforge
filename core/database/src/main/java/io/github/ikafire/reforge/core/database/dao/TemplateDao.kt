package io.github.ikafire.reforge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.ikafire.reforge.core.database.entity.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Query("SELECT * FROM workout_templates ORDER BY sortOrder ASC")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    fun getTemplateById(id: String): Flow<WorkoutTemplateEntity?>

    @Query("SELECT * FROM workout_templates WHERE folderId = :folderId ORDER BY sortOrder ASC")
    fun getTemplatesByFolder(folderId: String): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_templates WHERE folderId IS NULL ORDER BY sortOrder ASC")
    fun getTemplatesWithoutFolder(): Flow<List<WorkoutTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity)

    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)
}
