package io.github.ikafire.reforge.feature.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.reforge.core.domain.model.Exercise
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

data class CreateExerciseUiState(
    val name: String = "",
    val category: ExerciseCategory = ExerciseCategory.BARBELL,
    val primaryMuscle: MuscleGroup = MuscleGroup.CHEST,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val instructions: String = "",
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class CreateExerciseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val editExerciseId: String? = savedStateHandle["exerciseId"]

    private val _uiState = MutableStateFlow(CreateExerciseUiState(isEditing = editExerciseId != null))
    val uiState: StateFlow<CreateExerciseUiState> = _uiState.asStateFlow()

    init {
        if (editExerciseId != null) {
            viewModelScope.launch {
                exerciseRepository.getExerciseById(editExerciseId).collect { exercise ->
                    if (exercise != null) {
                        _uiState.value = _uiState.value.copy(
                            name = exercise.name,
                            category = exercise.category,
                            primaryMuscle = exercise.primaryMuscle,
                            secondaryMuscles = exercise.secondaryMuscles,
                            instructions = exercise.instructions ?: "",
                            isEditing = true,
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onCategoryChange(category: ExerciseCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun onPrimaryMuscleChange(muscle: MuscleGroup) {
        _uiState.value = _uiState.value.copy(primaryMuscle = muscle)
    }

    fun onSecondaryMusclesChange(muscles: List<MuscleGroup>) {
        _uiState.value = _uiState.value.copy(secondaryMuscles = muscles)
    }

    fun onInstructionsChange(instructions: String) {
        _uiState.value = _uiState.value.copy(instructions = instructions)
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            val exercise = Exercise(
                id = editExerciseId ?: UUID.randomUUID().toString(),
                name = state.name.trim(),
                category = state.category,
                primaryMuscle = state.primaryMuscle,
                secondaryMuscles = state.secondaryMuscles,
                instructions = state.instructions.ifBlank { null },
                isCustom = true,
                createdAt = Clock.System.now(),
            )

            if (editExerciseId != null) {
                exerciseRepository.updateExercise(exercise)
            } else {
                exerciseRepository.insertExercise(exercise)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
