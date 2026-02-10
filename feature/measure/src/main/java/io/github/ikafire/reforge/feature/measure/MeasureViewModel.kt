package io.github.ikafire.reforge.feature.measure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ikafire.reforge.core.domain.model.BodyMeasurement
import io.github.ikafire.reforge.core.domain.model.MeasurementType
import io.github.ikafire.reforge.core.domain.model.MeasurementUnit
import io.github.ikafire.reforge.core.domain.repository.BodyMeasurementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.util.UUID
import javax.inject.Inject

data class MeasureUiState(
    val latestByType: Map<MeasurementType, BodyMeasurement?> = emptyMap(),
    val selectedType: MeasurementType? = null,
    val selectedHistory: List<BodyMeasurement> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class MeasureViewModel @Inject constructor(
    private val repository: BodyMeasurementRepository,
) : ViewModel() {

    private val _selectedType = MutableStateFlow<MeasurementType?>(null)
    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<MeasureUiState> = combine(
        _selectedType,
        _refreshTrigger,
    ) { selectedType, _ ->
        val latestByType = MeasurementType.entries.associateWith { type ->
            repository.getLatestMeasurement(type).stateIn(viewModelScope, SharingStarted.Eagerly, null).value
        }

        val history = if (selectedType != null) {
            repository.getMeasurementsByType(selectedType).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
        } else {
            emptyList()
        }

        MeasureUiState(
            latestByType = latestByType,
            selectedType = selectedType,
            selectedHistory = history,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MeasureUiState(),
    )

    fun selectType(type: MeasurementType?) {
        _selectedType.value = type
    }

    fun logMeasurement(type: MeasurementType, value: Double) {
        viewModelScope.launch {
            val unit = when (type) {
                MeasurementType.WEIGHT -> MeasurementUnit.KG
                MeasurementType.BODY_FAT -> MeasurementUnit.PERCENT
                MeasurementType.CALORIC_INTAKE -> MeasurementUnit.KCAL
                else -> MeasurementUnit.CM
            }
            repository.insertMeasurement(
                BodyMeasurement(
                    id = UUID.randomUUID().toString(),
                    date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                    type = type,
                    value = value,
                    unit = unit,
                )
            )
            _refreshTrigger.value++
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch {
            repository.deleteMeasurement(id)
            _refreshTrigger.value++
        }
    }
}
