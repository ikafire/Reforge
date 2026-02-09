package io.github.ikafire.stronger.feature.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.ResistanceProfile
import io.github.ikafire.stronger.core.domain.model.ResistanceProfileType
import io.github.ikafire.stronger.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    val exercise: StateFlow<Exercise?> = exerciseRepository.getExerciseById(exerciseId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseRepository.updateExercise(exercise)
        }
    }

    fun deleteExercise() {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exerciseId)
        }
    }

    fun setResistanceProfile(type: ResistanceProfileType, multiplier: Double, notes: String?) {
        val current = exercise.value ?: return
        viewModelScope.launch {
            exerciseRepository.updateExercise(
                current.copy(
                    resistanceProfile = ResistanceProfile(
                        type = type,
                        multiplier = multiplier,
                        notes = notes,
                    )
                )
            )
        }
    }

    fun clearResistanceProfile() {
        val current = exercise.value ?: return
        viewModelScope.launch {
            exerciseRepository.updateExercise(current.copy(resistanceProfile = null))
        }
    }
}
