package io.github.ikafire.stronger.core.domain.repository

import io.github.ikafire.stronger.core.domain.model.LengthUnit
import io.github.ikafire.stronger.core.domain.model.ThemeMode
import io.github.ikafire.stronger.core.domain.model.UserPreferences
import io.github.ikafire.stronger.core.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setWeightUnit(weightUnit: WeightUnit)
    suspend fun setLengthUnit(lengthUnit: LengthUnit)
    suspend fun setOnboardingCompleted()
}
