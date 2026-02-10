package io.github.ikafire.stronger.feature.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.stronger.core.domain.model.Exercise
import io.github.ikafire.stronger.core.domain.model.ExerciseHistoryItem
import io.github.ikafire.stronger.core.domain.model.HistorySet
import io.github.ikafire.stronger.core.domain.model.ResistanceProfile
import io.github.ikafire.stronger.core.domain.model.ResistanceProfileType
import io.github.ikafire.stronger.core.domain.repository.ExerciseRepository
import io.github.ikafire.stronger.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject

data class OneRmDataPoint(
    val date: Instant,
    val oneRm: Double,
)

data class ChartDataPoint(
    val date: Instant,
    val value: Double,
)

data class ExerciseRecords(
    val maxWeight: Double? = null,
    val maxReps: Int? = null,
    val maxVolume: Double? = null,
    val maxOneRm: Double? = null,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val totalVolume: Double = 0.0,
)

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    val exercise: StateFlow<Exercise?> = exerciseRepository.getExerciseById(exerciseId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val _history = MutableStateFlow<List<ExerciseHistoryItem>>(emptyList())
    val history: StateFlow<List<ExerciseHistoryItem>> = _history.asStateFlow()

    private val _oneRmData = MutableStateFlow<List<OneRmDataPoint>>(emptyList())
    val oneRmData: StateFlow<List<OneRmDataPoint>> = _oneRmData.asStateFlow()

    private val _volumeData = MutableStateFlow<List<ChartDataPoint>>(emptyList())
    val volumeData: StateFlow<List<ChartDataPoint>> = _volumeData.asStateFlow()

    private val _bestSetData = MutableStateFlow<List<ChartDataPoint>>(emptyList())
    val bestSetData: StateFlow<List<ChartDataPoint>> = _bestSetData.asStateFlow()

    private val _totalRepsData = MutableStateFlow<List<ChartDataPoint>>(emptyList())
    val totalRepsData: StateFlow<List<ChartDataPoint>> = _totalRepsData.asStateFlow()

    private val _records = MutableStateFlow(ExerciseRecords())
    val records: StateFlow<ExerciseRecords> = _records.asStateFlow()

    init {
        loadExerciseHistory()
    }

    private fun loadExerciseHistory() {
        viewModelScope.launch {
            val items = workoutRepository.getExerciseHistory(exerciseId)
            _history.value = items

            // Compute 1RM data points (best 1RM per workout, using Epley formula)
            val oneRmPoints = items.mapNotNull { item ->
                val best1Rm = item.sets.maxOfOrNull { set -> computeOneRm(set) }
                if (best1Rm != null && best1Rm > 0) {
                    OneRmDataPoint(date = item.workoutDate, oneRm = best1Rm)
                } else null
            }.sortedBy { it.date }
            _oneRmData.value = oneRmPoints

            // Compute volume per workout
            _volumeData.value = items.map { item ->
                ChartDataPoint(
                    date = item.workoutDate,
                    value = item.sets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) },
                )
            }.sortedBy { it.date }

            // Compute best set per workout (highest weight)
            _bestSetData.value = items.mapNotNull { item ->
                val best = item.sets.maxOfOrNull { it.weight ?: 0.0 }
                if (best != null && best > 0) ChartDataPoint(date = item.workoutDate, value = best) else null
            }.sortedBy { it.date }

            // Compute total reps per workout
            _totalRepsData.value = items.map { item ->
                ChartDataPoint(
                    date = item.workoutDate,
                    value = item.sets.sumOf { it.reps ?: 0 }.toDouble(),
                )
            }.sortedBy { it.date }

            // Compute records
            val allSets = items.flatMap { it.sets }
            _records.value = ExerciseRecords(
                maxWeight = allSets.mapNotNull { it.weight }.maxOrNull(),
                maxReps = allSets.mapNotNull { it.reps }.maxOrNull(),
                maxVolume = items.maxOfOrNull { item ->
                    item.sets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
                },
                maxOneRm = oneRmPoints.maxOfOrNull { it.oneRm },
                totalSets = allSets.size,
                totalReps = allSets.sumOf { it.reps ?: 0 },
                totalVolume = allSets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) },
            )
        }
    }

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

internal fun computeOneRm(set: HistorySet): Double {
    val weight = set.effectiveWeight ?: set.weight ?: return 0.0
    val reps = set.reps ?: return 0.0
    if (reps <= 0 || weight <= 0) return 0.0
    if (reps == 1) return weight
    // Epley formula: 1RM = weight * (1 + reps / 30)
    return weight * (1 + reps / 30.0)
}
