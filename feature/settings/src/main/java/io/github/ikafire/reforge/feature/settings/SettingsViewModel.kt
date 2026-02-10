package io.github.ikafire.reforge.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.reforge.core.domain.model.LengthUnit
import io.github.ikafire.reforge.core.domain.model.ThemeMode
import io.github.ikafire.reforge.core.domain.model.UserPreferences
import io.github.ikafire.reforge.core.domain.model.WeightUnit
import io.github.ikafire.reforge.core.domain.repository.BodyMeasurementRepository
import io.github.ikafire.reforge.core.domain.repository.UserPreferencesRepository
import io.github.ikafire.reforge.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workoutRepository: WorkoutRepository,
    private val bodyMeasurementRepository: BodyMeasurementRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences(),
        )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setWeightUnit(weightUnit: WeightUnit, convertData: Boolean = false) {
        val currentUnit = preferences.value.weightUnit
        viewModelScope.launch {
            if (convertData && currentUnit != weightUnit) {
                val factor = if (weightUnit == WeightUnit.LBS) 2.20462 else 1.0 / 2.20462
                workoutRepository.convertAllWeights(factor)
                bodyMeasurementRepository.convertAllWeightMeasurements(factor)
            }
            userPreferencesRepository.setWeightUnit(weightUnit)
        }
    }

    fun setLengthUnit(lengthUnit: LengthUnit, convertData: Boolean = false) {
        val currentUnit = preferences.value.lengthUnit
        viewModelScope.launch {
            if (convertData && currentUnit != lengthUnit) {
                val factor = if (lengthUnit == LengthUnit.IN) 1.0 / 2.54 else 2.54
                bodyMeasurementRepository.convertAllLengthMeasurements(factor)
            }
            userPreferencesRepository.setLengthUnit(lengthUnit)
        }
    }

    fun setDefaultRestTimerSeconds(seconds: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setDefaultRestTimerSeconds(seconds)
        }
    }
}
