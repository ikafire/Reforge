package io.github.ikafire.reforge.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import io.github.ikafire.reforge.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import javax.inject.Inject

data class MuscleVolumeData(
    val muscle: MuscleGroup,
    val totalVolume: Double,
    val setCount: Int,
)

enum class TimePeriod(val label: String, val days: Int) {
    WEEK("Last 7 Days", 7),
    MONTH("Last 30 Days", 30),
    THREE_MONTHS("Last 3 Months", 90),
    ALL_TIME("All Time", 0),
}

data class AnalyticsUiState(
    val muscleVolumes: List<MuscleVolumeData> = emptyList(),
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val isLoading: Boolean = true,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun selectPeriod(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val period = _uiState.value.selectedPeriod
            val workouts = workoutRepository.getWorkoutHistory().first()

            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val cutoff = if (period.days > 0) {
                now.minus(period.days, DateTimeUnit.DAY, tz)
            } else null

            val filteredWorkouts = if (cutoff != null) {
                workouts.filter { it.startedAt >= cutoff }
            } else workouts

            // Build muscle volume map
            val muscleVolumes = mutableMapOf<MuscleGroup, Pair<Double, Int>>()

            for (workout in filteredWorkouts) {
                val workoutExercises = workoutRepository.getWorkoutExercises(workout.id).first()
                for (we in workoutExercises) {
                    val exercise = exerciseRepository.getExerciseById(we.exerciseId).first() ?: continue
                    val sets = workoutRepository.getWorkoutSets(we.id).first().filter { it.isCompleted }

                    val volume = sets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
                    val setCount = sets.size

                    val primaryMuscle = exercise.primaryMuscle
                    val current = muscleVolumes[primaryMuscle] ?: (0.0 to 0)
                    muscleVolumes[primaryMuscle] = (current.first + volume) to (current.second + setCount)

                    for (secondary in exercise.secondaryMuscles) {
                        val curr = muscleVolumes[secondary] ?: (0.0 to 0)
                        muscleVolumes[secondary] = (curr.first + volume * 0.5) to (curr.second + setCount)
                    }
                }
            }

            val volumes = muscleVolumes.map { (muscle, data) ->
                MuscleVolumeData(muscle = muscle, totalVolume = data.first, setCount = data.second)
            }.sortedByDescending { it.totalVolume }

            _uiState.value = _uiState.value.copy(
                muscleVolumes = volumes,
                isLoading = false,
            )
        }
    }
}
