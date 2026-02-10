package io.github.ikafire.reforge.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.SetType
import io.github.ikafire.reforge.core.domain.model.TemplateExercise
import io.github.ikafire.reforge.core.domain.model.TemplateFolder
import io.github.ikafire.reforge.core.domain.model.Workout
import io.github.ikafire.reforge.core.domain.model.WorkoutExercise
import io.github.ikafire.reforge.core.domain.model.WorkoutSet
import io.github.ikafire.reforge.core.domain.model.WorkoutTemplate
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.github.ikafire.reforge.core.domain.repository.TemplateRepository
import io.github.ikafire.reforge.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

data class TemplateWithExercises(
    val template: WorkoutTemplate,
    val exercises: List<Pair<TemplateExercise, Exercise?>>,
)

data class TemplateListUiState(
    val folders: List<TemplateFolder> = emptyList(),
    val templatesByFolder: Map<String?, List<TemplateWithExercises>> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    val uiState: StateFlow<TemplateListUiState> = combine(
        templateRepository.getAllTemplates(),
        templateRepository.getAllFolders(),
    ) { templates, folders ->
        val templateWithExercises = templates.map { template ->
            val exercises = templateRepository.getTemplateExercises(template.id).first()
            val withDetails = exercises.map { te ->
                val exercise = exerciseRepository.getExerciseById(te.exerciseId).first()
                te to exercise
            }
            TemplateWithExercises(template, withDetails)
        }

        val byFolder = templateWithExercises.groupBy { it.template.folderId }

        TemplateListUiState(
            folders = folders,
            templatesByFolder = byFolder,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TemplateListUiState(),
    )

    fun createTemplate(name: String) {
        viewModelScope.launch {
            val template = WorkoutTemplate(
                id = UUID.randomUUID().toString(),
                name = name,
            )
            templateRepository.insertTemplate(template)
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val folder = TemplateFolder(
                id = UUID.randomUUID().toString(),
                name = name,
            )
            templateRepository.insertFolder(folder)
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(id)
        }
    }

    fun duplicateTemplate(id: String) {
        viewModelScope.launch {
            templateRepository.duplicateTemplate(id)
        }
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            templateRepository.deleteFolder(id)
        }
    }

    fun moveTemplate(templateId: String, folderId: String?) {
        viewModelScope.launch {
            val template = templateRepository.getTemplateById(templateId).first() ?: return@launch
            templateRepository.updateTemplate(template.copy(folderId = folderId))
        }
    }

    fun addExerciseToTemplate(templateId: String, exerciseId: String) {
        viewModelScope.launch {
            val existing = templateRepository.getTemplateExercises(templateId).first()
            val te = TemplateExercise(
                id = UUID.randomUUID().toString(),
                templateId = templateId,
                exerciseId = exerciseId,
                sortOrder = existing.size,
            )
            templateRepository.insertTemplateExercise(te)
        }
    }

    fun startWorkoutFromTemplate(templateId: String) {
        viewModelScope.launch {
            val activeWorkout = workoutRepository.getActiveWorkout().first()
            if (activeWorkout != null) return@launch

            val template = templateRepository.getTemplateById(templateId).first() ?: return@launch
            val templateExercises = templateRepository.getTemplateExercises(templateId).first()

            val workoutId = UUID.randomUUID().toString()
            val workout = Workout(
                id = workoutId,
                templateId = templateId,
                name = template.name,
                startedAt = Clock.System.now(),
                isActive = true,
            )
            workoutRepository.startWorkout(workout)

            templateExercises.forEach { te ->
                val weId = UUID.randomUUID().toString()
                val we = WorkoutExercise(
                    id = weId,
                    workoutId = workoutId,
                    exerciseId = te.exerciseId,
                    sortOrder = te.sortOrder,
                    supersetGroup = te.supersetGroup,
                )
                workoutRepository.addExerciseToWorkout(we)

                repeat(te.targetSets) { setIndex ->
                    val set = WorkoutSet(
                        id = UUID.randomUUID().toString(),
                        workoutExerciseId = weId,
                        sortOrder = setIndex,
                        type = SetType.WORKING,
                    )
                    workoutRepository.insertSet(set)
                }
            }
        }
    }
}
