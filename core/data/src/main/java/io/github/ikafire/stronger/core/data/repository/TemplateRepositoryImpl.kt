package io.github.ikafire.stronger.core.data.repository

import io.github.ikafire.stronger.core.common.generateUuid
import io.github.ikafire.stronger.core.data.mapper.toDomain
import io.github.ikafire.stronger.core.data.mapper.toEntity
import io.github.ikafire.stronger.core.database.dao.TemplateDao
import io.github.ikafire.stronger.core.database.dao.TemplateExerciseDao
import io.github.ikafire.stronger.core.database.dao.TemplateFolderDao
import io.github.ikafire.stronger.core.domain.model.TemplateExercise
import io.github.ikafire.stronger.core.domain.model.TemplateFolder
import io.github.ikafire.stronger.core.domain.model.WorkoutTemplate
import io.github.ikafire.stronger.core.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao,
    private val templateFolderDao: TemplateFolderDao,
    private val templateExerciseDao: TemplateExerciseDao,
) : TemplateRepository {

    override fun getAllTemplates(): Flow<List<WorkoutTemplate>> =
        templateDao.getAllTemplates().map { list -> list.map { it.toDomain() } }

    override fun getTemplateById(id: String): Flow<WorkoutTemplate?> =
        templateDao.getTemplateById(id).map { it?.toDomain() }

    override fun getTemplateExercises(templateId: String): Flow<List<TemplateExercise>> =
        templateExerciseDao.getTemplateExercises(templateId).map { list -> list.map { it.toDomain() } }

    override fun getAllFolders(): Flow<List<TemplateFolder>> =
        templateFolderDao.getAllFolders().map { list -> list.map { it.toDomain() } }

    override suspend fun insertTemplate(template: WorkoutTemplate) =
        templateDao.insertTemplate(template.toEntity())

    override suspend fun updateTemplate(template: WorkoutTemplate) =
        templateDao.updateTemplate(template.toEntity())

    override suspend fun deleteTemplate(id: String) =
        templateDao.deleteTemplate(id)

    override suspend fun duplicateTemplate(id: String): String {
        val template = templateDao.getTemplateById(id).first() ?: error("Template not found")
        val exercises = templateExerciseDao.getTemplateExercises(id).first()
        val newId = generateUuid()
        val newTemplate = template.copy(id = newId, name = "${template.name} (copy)")
        templateDao.insertTemplate(newTemplate)
        val newExercises = exercises.map { it.copy(id = generateUuid(), templateId = newId) }
        templateExerciseDao.insertTemplateExercises(newExercises)
        return newId
    }

    override suspend fun insertFolder(folder: TemplateFolder) =
        templateFolderDao.insertFolder(folder.toEntity())

    override suspend fun updateFolder(folder: TemplateFolder) =
        templateFolderDao.updateFolder(folder.toEntity())

    override suspend fun deleteFolder(id: String) =
        templateFolderDao.deleteFolder(id)

    override suspend fun insertTemplateExercise(exercise: TemplateExercise) =
        templateExerciseDao.insertTemplateExercise(exercise.toEntity())

    override suspend fun updateTemplateExercise(exercise: TemplateExercise) =
        templateExerciseDao.updateTemplateExercise(exercise.toEntity())

    override suspend fun deleteTemplateExercise(id: String) =
        templateExerciseDao.deleteTemplateExercise(id)
}
