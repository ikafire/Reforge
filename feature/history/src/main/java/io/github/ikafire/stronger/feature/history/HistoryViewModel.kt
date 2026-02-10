package io.github.ikafire.stronger.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class WorkoutSummary(
    val workout: Workout,
    val exerciseNames: List<String>,
    val totalSets: Int,
    val totalVolume: Double,
)

data class WorkoutDetailData(
    val workout: Workout,
    val exercises: List<WorkoutExerciseDetail>,
)

data class WorkoutExerciseDetail(
    val exerciseName: String,
    val sets: List<WorkoutSet>,
)

data class HistoryUiState(
    val workoutsByMonth: Map<String, List<WorkoutSummary>> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val selectedWorkout: WorkoutDetailData? = null,
    val showCalendar: Boolean = false,
    val workoutDates: Set<LocalDate> = emptySet(),
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedWorkout = MutableStateFlow<WorkoutDetailData?>(null)
    private val _showCalendar = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(
        workoutRepository.getWorkoutHistory(),
        _searchQuery,
        _selectedWorkout,
        _showCalendar,
    ) { workouts, query, selectedWorkout, showCalendar ->
        val summaries = workouts.map { workout ->
            val exercises = workoutRepository.getWorkoutExercises(workout.id).first()
            val exerciseNames = exercises.map { we ->
                exerciseRepository.getExerciseById(we.exerciseId).first()?.name ?: "Unknown"
            }
            val sets = exercises.flatMap { we ->
                workoutRepository.getWorkoutSets(we.id).first()
            }
            val totalVolume = sets.filter { it.isCompleted }.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
            WorkoutSummary(workout, exerciseNames, sets.count { it.isCompleted }, totalVolume)
        }

        val filtered = if (query.isBlank()) summaries else {
            summaries.filter { summary ->
                summary.exerciseNames.any { it.contains(query, ignoreCase = true) } ||
                    (summary.workout.name?.contains(query, ignoreCase = true) == true)
            }
        }

        val grouped = filtered.groupBy { summary ->
            val date = summary.workout.startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
            "${date.year}-${date.monthNumber.toString().padStart(2, '0')}"
        }

        val workoutDates = workouts.map { workout ->
            workout.startedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.toSet()

        HistoryUiState(
            workoutsByMonth = grouped,
            searchQuery = query,
            isLoading = false,
            selectedWorkout = selectedWorkout,
            showCalendar = showCalendar,
            workoutDates = workoutDates,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectWorkout(workoutId: String) {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId).first() ?: return@launch
            val workoutExercises = workoutRepository.getWorkoutExercises(workoutId).first()
            val exercises = workoutExercises.map { we ->
                val name = exerciseRepository.getExerciseById(we.exerciseId).first()?.name ?: "Unknown"
                val sets = workoutRepository.getWorkoutSets(we.id).first()
                WorkoutExerciseDetail(exerciseName = name, sets = sets)
            }
            _selectedWorkout.value = WorkoutDetailData(workout = workout, exercises = exercises)
        }
    }

    fun clearSelectedWorkout() {
        _selectedWorkout.value = null
    }

    fun toggleCalendar() {
        _showCalendar.value = !_showCalendar.value
    }

    fun hideCalendar() {
        _showCalendar.value = false
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
        }
    }
}
