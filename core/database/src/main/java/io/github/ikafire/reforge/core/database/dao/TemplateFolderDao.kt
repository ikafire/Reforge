package io.github.ikafire.reforge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.ikafire.reforge.core.database.entity.TemplateFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateFolderDao {

    @Query("SELECT * FROM template_folders ORDER BY sortOrder ASC")
    fun getAllFolders(): Flow<List<TemplateFolderEntity>>

    @Query("SELECT * FROM template_folders WHERE id = :id")
    fun getFolderById(id: String): Flow<TemplateFolderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: TemplateFolderEntity)

    @Update
    suspend fun updateFolder(folder: TemplateFolderEntity)

    @Query("DELETE FROM template_folders WHERE id = :id")
    suspend fun deleteFolder(id: String)
}
