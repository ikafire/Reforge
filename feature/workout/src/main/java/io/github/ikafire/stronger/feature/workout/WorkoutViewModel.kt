package io.github.ikafire.stronger.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.SetType
import io.github.ikafire.stronger.core.domain.model.Workout
import io.github.ikafire.stronger.core.domain.model.WorkoutExercise
import io.github.ikafire.stronger.core.domain.model.WorkoutSet
import io.github.ikafire.stronger.core.domain.repository.ExerciseRepository
import io.github.ikafire.stronger.core.domain.repository.WorkoutRepository
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

data class WorkoutExerciseWithDetails(
    val workoutExercise: WorkoutExercise,
    val exercise: Exercise?,
    val sets: List<WorkoutSet>,
    val previousSets: List<WorkoutSet>,
)

data class WorkoutUiState(
    val activeWorkout: Workout? = null,
    val exercises: List<WorkoutExerciseWithDetails> = emptyList(),
    val isLoading: Boolean = true,
    val showExercisePicker: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showFinishDialog: Boolean = false,
    val restTimerSeconds: Int? = null,
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _showExercisePicker = MutableStateFlow(false)
    private val _showDiscardDialog = MutableStateFlow(false)
    private val _showFinishDialog = MutableStateFlow(false)
    private val _exerciseDetails = MutableStateFlow<List<WorkoutExerciseWithDetails>>(emptyList())

    val uiState: StateFlow<WorkoutUiState> = combine(
        workoutRepository.getActiveWorkout(),
        _exerciseDetails,
        _showExercisePicker,
        _showDiscardDialog,
        _showFinishDialog,
    ) { workout, exercises, showPicker, showDiscard, showFinish ->
        WorkoutUiState(
            activeWorkout = workout,
            exercises = exercises,
            isLoading = false,
            showExercisePicker = showPicker,
            showDiscardDialog = showDiscard,
            showFinishDialog = showFinish,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutUiState(),
    )

    init {
        viewModelScope.launch {
            workoutRepository.getActiveWorkout().collect { workout ->
                if (workout != null) {
                    loadExercises(workout.id)
                } else {
                    _exerciseDetails.value = emptyList()
                }
            }
        }
    }

    private fun loadExercises(workoutId: String) {
        viewModelScope.launch {
            workoutRepository.getWorkoutExercises(workoutId).collect { workoutExercises ->
                val details = workoutExercises.map { we ->
                    val exercise = exerciseRepository.getExerciseById(we.exerciseId).first()
                    val sets = workoutRepository.getWorkoutSets(we.id).first()
                    WorkoutExerciseWithDetails(
                        workoutExercise = we,
                        exercise = exercise,
                        sets = sets,
                        previousSets = emptyList(),
                    )
                }
                _exerciseDetails.value = details
            }
        }
    }

    fun startEmptyWorkout() {
        viewModelScope.launch {
            val activeWorkout = workoutRepository.getActiveWorkout().first()
            if (activeWorkout != null) return@launch

            val workout = Workout(
                id = UUID.randomUUID().toString(),
                startedAt = Clock.System.now(),
                isActive = true,
            )
            workoutRepository.startWorkout(workout)
        }
    }

    fun showExercisePicker() {
        _showExercisePicker.value = true
    }

    fun hideExercisePicker() {
        _showExercisePicker.value = false
    }

    fun addExercises(exercises: List<Exercise>) {
        val workout = uiState.value.activeWorkout ?: return
        viewModelScope.launch {
            val currentCount = _exerciseDetails.value.size
            exercises.forEachIndexed { index, exercise ->
                val workoutExercise = WorkoutExercise(
                    id = UUID.randomUUID().toString(),
                    workoutId = workout.id,
                    exerciseId = exercise.id,
                    sortOrder = currentCount + index,
                )
                workoutRepository.addExerciseToWorkout(workoutExercise)

                val set = WorkoutSet(
                    id = UUID.randomUUID().toString(),
                    workoutExerciseId = workoutExercise.id,
                    sortOrder = 0,
                    type = SetType.WORKING,
                )
                workoutRepository.insertSet(set)
            }
            _showExercisePicker.value = false
        }
    }

    fun addSet(workoutExerciseId: String) {
        viewModelScope.launch {
            val currentSets = workoutRepository.getWorkoutSets(workoutExerciseId).first()
            val lastSet = currentSets.lastOrNull()
            val set = WorkoutSet(
                id = UUID.randomUUID().toString(),
                workoutExerciseId = workoutExerciseId,
                sortOrder = currentSets.size,
                type = SetType.WORKING,
                weight = lastSet?.weight,
            )
            workoutRepository.insertSet(set)
        }
    }

    fun removeSet(setId: String) {
        viewModelScope.launch {
            workoutRepository.deleteSet(setId)
        }
    }

    fun updateSet(set: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.updateSet(set)
        }
    }

    fun completeSet(set: WorkoutSet) {
        viewModelScope.launch {
            val exerciseDetail = _exerciseDetails.value.find { detail ->
                detail.sets.any { it.id == set.id }
            }
            val exercise = exerciseDetail?.exercise
            val multiplier = exercise?.resistanceProfile?.multiplier ?: 1.0
            val effectiveWeight = set.weight?.let { it * multiplier }

            workoutRepository.updateSet(
                set.copy(
                    isCompleted = true,
                    completedAt = Clock.System.now(),
                    effectiveWeight = effectiveWeight,
                )
            )
        }
    }

    fun uncompleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.updateSet(
                set.copy(
                    isCompleted = false,
                    completedAt = null,
                    effectiveWeight = null,
                )
            )
        }
    }

    fun setSetType(set: WorkoutSet, type: SetType) {
        viewModelScope.launch {
            workoutRepository.updateSet(set.copy(type = type))
        }
    }

    fun removeExercise(workoutExerciseId: String) {
        viewModelScope.launch {
            workoutRepository.removeExerciseFromWorkout(workoutExerciseId)
        }
    }

    fun updateWorkoutNotes(notes: String) {
        val workout = uiState.value.activeWorkout ?: return
        viewModelScope.launch {
            workoutRepository.updateWorkout(workout.copy(notes = notes.ifBlank { null }))
        }
    }

    fun updateExerciseNotes(workoutExercise: WorkoutExercise, notes: String) {
        viewModelScope.launch {
            workoutRepository.updateWorkoutExercise(
                workoutExercise.copy(notes = notes.ifBlank { null })
            )
        }
    }

    fun showFinishDialog() {
        _showFinishDialog.value = true
    }

    fun hideFinishDialog() {
        _showFinishDialog.value = false
    }

    fun finishWorkout(discardIncomplete: Boolean) {
        val workout = uiState.value.activeWorkout ?: return
        viewModelScope.launch {
            if (discardIncomplete) {
                _exerciseDetails.value.forEach { detail ->
                    detail.sets.filter { !it.isCompleted }.forEach {
                        workoutRepository.deleteSet(it.id)
                    }
                }
            }
            workoutRepository.finishWorkout(workout.id)
            _showFinishDialog.value = false
        }
    }

    fun showDiscardDialog() {
        _showDiscardDialog.value = true
    }

    fun hideDiscardDialog() {
        _showDiscardDialog.value = false
    }

    fun discardWorkout() {
        val workout = uiState.value.activeWorkout ?: return
        viewModelScope.launch {
            workoutRepository.discardWorkout(workout.id)
            _showDiscardDialog.value = false
        }
    }

    fun createSuperset(exerciseIds: List<String>) {
        val workout = uiState.value.activeWorkout ?: return
        viewModelScope.launch {
            val groupId = (_exerciseDetails.value.mapNotNull { it.workoutExercise.supersetGroup }.maxOrNull() ?: 0) + 1
            exerciseIds.forEach { id ->
                val we = _exerciseDetails.value.find { it.workoutExercise.id == id }?.workoutExercise ?: return@forEach
                workoutRepository.updateWorkoutExercise(we.copy(supersetGroup = groupId))
            }
        }
    }

    fun reorderExercise(fromIndex: Int, toIndex: Int) {
        val workout = uiState.value.activeWorkout ?: return
        viewModelScope.launch {
            val exercises = _exerciseDetails.value.toMutableList()
            if (fromIndex in exercises.indices && toIndex in exercises.indices) {
                val item = exercises.removeAt(fromIndex)
                exercises.add(toIndex, item)
                exercises.forEachIndexed { index, detail ->
                    workoutRepository.updateWorkoutExercise(
                        detail.workoutExercise.copy(sortOrder = index)
                    )
                }
            }
        }
    }
}
