package io.github.ikafire.reforge.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.reforge.core.domain.model.ExerciseCategory
import io.github.ikafire.reforge.core.domain.model.ExerciseWithUsage
import io.github.ikafire.reforge.core.domain.model.MuscleGroup
import io.github.ikafire.reforge.core.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class UsageGroup(val label: String, val range: IntRange) {
    HEAVY("50+ times", 50..Int.MAX_VALUE),
    FREQUENT("26-50 times", 26..50),
    MODERATE("11-25 times", 11..25),
    LIGHT("1-10 times", 1..10),
    NEVER("Never used", 0..0),
}

data class ExerciseListUiState(
    val groupedExercises: Map<UsageGroup, List<ExerciseWithUsage>> = emptyMap(),
    val searchQuery: String = "",
    val selectedCategory: ExerciseCategory? = null,
    val selectedMuscle: MuscleGroup? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ExerciseCategory?>(null)
    private val selectedMuscle = MutableStateFlow<MuscleGroup?>(null)

    val uiState: StateFlow<ExerciseListUiState> = combine(
        exerciseRepository.getAllExercisesWithUsageCount(),
        searchQuery,
        selectedCategory,
        selectedMuscle,
    ) { exercises, query, category, muscle ->
        val filtered = exercises.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.exercise.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null ||
                item.exercise.category == category
            val matchesMuscle = muscle == null ||
                item.exercise.primaryMuscle == muscle ||
                muscle in item.exercise.secondaryMuscles
            matchesQuery && matchesCategory && matchesMuscle
        }

        val grouped = UsageGroup.entries.associateWith { group ->
            filtered.filter { it.usageCount in group.range }
        }.filterValues { it.isNotEmpty() }

        ExerciseListUiState(
            groupedExercises = grouped,
            searchQuery = query,
            selectedCategory = category,
            selectedMuscle = muscle,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExerciseListUiState(),
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(category: ExerciseCategory?) {
        selectedCategory.value = category
    }

    fun onMuscleSelected(muscle: MuscleGroup?) {
        selectedMuscle.value = muscle
    }
}
