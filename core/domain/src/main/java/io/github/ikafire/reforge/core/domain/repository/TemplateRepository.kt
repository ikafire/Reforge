package io.github.ikafire.reforge.core.domain.repository

import io.github.ikafire.reforge.core.domain.model.TemplateExercise
import io.github.ikafire.reforge.core.domain.model.TemplateFolder
import io.github.ikafire.reforge.core.domain.model.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>
    fun getTemplateById(id: String): Flow<WorkoutTemplate?>
    fun getTemplateExercises(templateId: String): Flow<List<TemplateExercise>>
    fun getAllFolders(): Flow<List<TemplateFolder>>
    suspend fun insertTemplate(template: WorkoutTemplate)
    suspend fun updateTemplate(template: WorkoutTemplate)
    suspend fun deleteTemplate(id: String)
    suspend fun duplicateTemplate(id: String): String
    suspend fun insertFolder(folder: TemplateFolder)
    suspend fun updateFolder(folder: TemplateFolder)
    suspend fun deleteFolder(id: String)
    suspend fun insertTemplateExercise(exercise: TemplateExercise)
    suspend fun updateTemplateExercise(exercise: TemplateExercise)
    suspend fun deleteTemplateExercise(id: String)
}
