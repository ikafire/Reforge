package io.github.ikafire.stronger.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.stronger.core.domain.model.LengthUnit
import io.github.ikafire.stronger.core.domain.model.ThemeMode
import io.github.ikafire.stronger.core.domain.model.UserPreferences
import io.github.ikafire.stronger.core.domain.model.WeightUnit
import io.github.ikafire.stronger.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
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

    fun setWeightUnit(weightUnit: WeightUnit) {
        viewModelScope.launch {
            userPreferencesRepository.setWeightUnit(weightUnit)
        }
    }

    fun setLengthUnit(lengthUnit: LengthUnit) {
        viewModelScope.launch {
            userPreferencesRepository.setLengthUnit(lengthUnit)
        }
    }
}
