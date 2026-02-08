package io.github.ikafire.stronger.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.ikafire.stronger.core.domain.model.LengthUnit
import io.github.ikafire.stronger.core.domain.model.ThemeMode
import io.github.ikafire.stronger.core.domain.model.UserPreferences
import io.github.ikafire.stronger.core.domain.model.WeightUnit
import io.github.ikafire.stronger.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val LENGTH_UNIT = stringPreferencesKey("length_unit")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM,
            weightUnit = prefs[Keys.WEIGHT_UNIT]?.let { WeightUnit.valueOf(it) } ?: WeightUnit.KG,
            lengthUnit = prefs[Keys.LENGTH_UNIT]?.let { LengthUnit.valueOf(it) } ?: LengthUnit.CM,
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
        )
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    override suspend fun setWeightUnit(weightUnit: WeightUnit) {
        context.dataStore.edit { it[Keys.WEIGHT_UNIT] = weightUnit.name }
    }

    override suspend fun setLengthUnit(lengthUnit: LengthUnit) {
        context.dataStore.edit { it[Keys.LENGTH_UNIT] = lengthUnit.name }
    }

    override suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }
}
