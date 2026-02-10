package io.github.ikafire.stronger.feature.settings

import io.github.ikafire.stronger.core.domain.model.LengthUnit
import io.github.ikafire.stronger.core.domain.model.UserPreferences
import io.github.ikafire.stronger.core.domain.model.WeightUnit
import io.github.ikafire.stronger.core.domain.repository.BodyMeasurementRepository
import io.github.ikafire.stronger.core.domain.repository.UserPreferencesRepository
import io.github.ikafire.stronger.core.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnitConversionTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val prefsFlow = MutableStateFlow(UserPreferences())
    private val userPrefsRepo = mockk<UserPreferencesRepository>(relaxed = true)
    private val workoutRepo = mockk<WorkoutRepository>(relaxed = true)
    private val bodyMeasurementRepo = mockk<BodyMeasurementRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { userPrefsRepo.userPreferences } returns prefsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(userPrefsRepo, workoutRepo, bodyMeasurementRepo)

    // 7.1 KG → LBS factor ≈ 2.20462
    @Test
    fun `KG to LBS conversion uses correct factor`() = runTest {
        prefsFlow.value = UserPreferences(weightUnit = WeightUnit.KG)
        val vm = createViewModel()
        // Collect to activate stateIn
        val job = backgroundScope.launch { vm.preferences.collect {} }
        advanceUntilIdle()

        vm.setWeightUnit(WeightUnit.LBS, convertData = true)
        advanceUntilIdle()

        coVerify { workoutRepo.convertAllWeights(withArg { factor ->
            assertEquals(2.20462, factor, 0.001)
        }) }
        job.cancel()
    }

    // 7.2 LBS → KG factor ≈ 1/2.20462
    @Test
    fun `LBS to KG conversion uses correct factor`() = runTest {
        prefsFlow.value = UserPreferences(weightUnit = WeightUnit.LBS)
        val vm = createViewModel()
        val job = backgroundScope.launch { vm.preferences.collect {} }
        advanceUntilIdle()

        vm.setWeightUnit(WeightUnit.KG, convertData = true)
        advanceUntilIdle()

        coVerify { workoutRepo.convertAllWeights(withArg { factor ->
            assertEquals(1.0 / 2.20462, factor, 0.001)
        }) }
        job.cancel()
    }

    // 7.3 CM → IN factor ≈ 1/2.54
    @Test
    fun `CM to IN conversion uses correct factor`() = runTest {
        prefsFlow.value = UserPreferences(lengthUnit = LengthUnit.CM)
        val vm = createViewModel()
        val job = backgroundScope.launch { vm.preferences.collect {} }
        advanceUntilIdle()

        vm.setLengthUnit(LengthUnit.IN, convertData = true)
        advanceUntilIdle()

        coVerify { bodyMeasurementRepo.convertAllLengthMeasurements(withArg { factor ->
            assertEquals(1.0 / 2.54, factor, 0.001)
        }) }
        job.cancel()
    }

    // 7.4 IN → CM factor ≈ 2.54
    @Test
    fun `IN to CM conversion uses correct factor`() = runTest {
        prefsFlow.value = UserPreferences(lengthUnit = LengthUnit.IN)
        val vm = createViewModel()
        val job = backgroundScope.launch { vm.preferences.collect {} }
        advanceUntilIdle()

        vm.setLengthUnit(LengthUnit.CM, convertData = true)
        advanceUntilIdle()

        coVerify { bodyMeasurementRepo.convertAllLengthMeasurements(withArg { factor ->
            assertEquals(2.54, factor, 0.001)
        }) }
        job.cancel()
    }
}
