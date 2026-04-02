package com.kk.calorietracker.ui.food

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.calorietracker.data.model.FoodItem
import com.kk.calorietracker.data.model.MeasurementUnit
import com.kk.calorietracker.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodUiState(
    val editingId: Long? = null,
    val name: String = "",
    val measurementUnit: MeasurementUnit = MeasurementUnit.PER_GRAM,
    val measurementQuantity: String = "100",
    val fatG: String = "",
    val carbsG: String = "",
    val proteinG: String = "",
    val showDeleteConfirm: FoodItem? = null,
    val referencedFoodItemIds: Set<Long> = emptySet(),
    val items: List<FoodItem> = emptyList(),
)

sealed interface FoodEvent {
    data class ShowSnackbar(val message: String) : FoodEvent
}

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val repository: FoodRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FoodUiState())
    val state: StateFlow<FoodUiState> = _state

    private val _events = Channel<FoodEvent>()
    val events = _events.receiveAsFlow()

    val items: StateFlow<List<FoodItem>> = repository.getAllFoodItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.getReferencedFoodItemIds().collect { referencedIds ->
                _state.update { it.copy(referencedFoodItemIds = referencedIds) }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onUnitChange(unit: MeasurementUnit) = _state.update { it.copy(measurementUnit = unit) }
    fun onQuantityChange(value: String) = _state.update { it.copy(measurementQuantity = value) }
    fun onFatChange(value: String) = _state.update { it.copy(fatG = value) }
    fun onCarbsChange(value: String) = _state.update { it.copy(carbsG = value) }
    fun onProteinChange(value: String) = _state.update { it.copy(proteinG = value) }

    fun onEditItem(item: FoodItem) {
        _state.update {
            it.copy(
                editingId = item.id,
                name = item.name,
                measurementUnit = item.measurementUnit,
                measurementQuantity = item.measurementQuantity.toString(),
                fatG = item.fatG.toString(),
                carbsG = item.carbsG.toString(),
                proteinG = item.proteinG.toString(),
            )
        }
    }

    fun onDeleteRequest(item: FoodItem) = _state.update { it.copy(showDeleteConfirm = item) }
    fun onDeleteDismiss() = _state.update { it.copy(showDeleteConfirm = null) }

    fun onDeleteConfirm(item: FoodItem, successMsg: String, blockedMsg: String, failedMsg: String) {
        _state.update { it.copy(showDeleteConfirm = null) }
        viewModelScope.launch {
            try {
                repository.deleteFoodItem(item)
                _events.send(FoodEvent.ShowSnackbar(successMsg))
            } catch (_: SQLiteConstraintException) {
                _events.send(FoodEvent.ShowSnackbar(blockedMsg))
            } catch (_: Exception) {
                _events.send(FoodEvent.ShowSnackbar(failedMsg))
            }
        }
    }

    fun onSave(savedMsg: String, errorMsg: String) {
        val s = _state.value
        val qty = s.measurementQuantity.toDoubleOrNull()
        val fat = s.fatG.toDoubleOrNull()
        val carbs = s.carbsG.toDoubleOrNull()
        val protein = s.proteinG.toDoubleOrNull()

        if (s.name.isBlank() || qty == null || fat == null || carbs == null || protein == null) {
            viewModelScope.launch { _events.send(FoodEvent.ShowSnackbar(errorMsg)) }
            return
        }

        viewModelScope.launch {
            repository.saveFoodItem(
                FoodItem(
                    id = s.editingId ?: 0,
                    name = s.name.trim(),
                    measurementUnit = s.measurementUnit,
                    measurementQuantity = qty,
                    fatG = fat,
                    carbsG = carbs,
                    proteinG = protein,
                )
            )
            _state.update { FoodUiState() }
            _events.send(FoodEvent.ShowSnackbar(savedMsg))
        }
    }

    fun onClearForm() = _state.update { FoodUiState() }
}
