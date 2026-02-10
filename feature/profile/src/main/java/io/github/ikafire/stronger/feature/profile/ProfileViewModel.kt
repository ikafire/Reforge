package io.github.ikafire.stronger.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.stronger.core.domain.model.Workout
import io.github.ikafire.stronger.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class ProfileUiState(
    val totalWorkouts: Int = 0,
    val workoutsPerWeek: List<Int> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = workoutRepository.getWorkoutHistory()
        .map { workouts ->
            val now = Clock.System.now()
            val tz = TimeZone.currentSystemDefault()
            val weekCounts = (0 until 8).map { weekOffset ->
                val weekStart = now.minus(7 * (weekOffset + 1), DateTimeUnit.DAY, tz)
                val weekEnd = now.minus(7 * weekOffset, DateTimeUnit.DAY, tz)
                workouts.count { it.startedAt in weekStart..weekEnd }
            }.reversed()

            ProfileUiState(
                totalWorkouts = workouts.size,
                workoutsPerWeek = weekCounts,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(),
        )
}
