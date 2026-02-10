package io.github.ikafire.reforge.core.data.mapper

import io.github.ikafire.reforge.core.database.entity.TemplateFolderEntity
import io.github.ikafire.reforge.core.database.entity.TemplateExerciseEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutTemplateEntity
import io.github.ikafire.reforge.core.domain.model.TemplateExercise
import io.github.ikafire.reforge.core.domain.model.TemplateFolder
import io.github.ikafire.reforge.core.domain.model.WorkoutTemplate

fun WorkoutTemplateEntity.toDomain(): WorkoutTemplate = WorkoutTemplate(
    id = id,
    name = name,
    folderId = folderId,
    sortOrder = sortOrder,
)

fun WorkoutTemplate.toEntity(): WorkoutTemplateEntity = WorkoutTemplateEntity(
    id = id,
    name = name,
    folderId = folderId,
    sortOrder = sortOrder,
)

fun TemplateFolderEntity.toDomain(): TemplateFolder = TemplateFolder(
    id = id,
    name = name,
    sortOrder = sortOrder,
)

fun TemplateFolder.toEntity(): TemplateFolderEntity = TemplateFolderEntity(
    id = id,
    name = name,
    sortOrder = sortOrder,
)

fun TemplateExerciseEntity.toDomain(): TemplateExercise = TemplateExercise(
    id = id,
    templateId = templateId,
    exerciseId = exerciseId,
    sortOrder = sortOrder,
    targetSets = targetSets,
    targetReps = targetReps,
    supersetGroup = supersetGroup,
)

fun TemplateExercise.toEntity(): TemplateExerciseEntity = TemplateExerciseEntity(
    id = id,
    templateId = templateId,
    exerciseId = exerciseId,
    sortOrder = sortOrder,
    targetSets = targetSets,
    targetReps = targetReps,
    supersetGroup = supersetGroup,
)
