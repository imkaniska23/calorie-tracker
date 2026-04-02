package com.kk.calorietracker.ui.targets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.calorietracker.data.model.DailyTarget
import com.kk.calorietracker.data.repository.TargetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TargetsUiState(
    val calories: String = "",
    val fatG: String = "",
    val carbsG: String = "",
    val proteinG: String = "",
)

sealed interface TargetsEvent {
    data class ShowSnackbar(val message: String) : TargetsEvent
    data object NavigateToTrends : TargetsEvent
}

@HiltViewModel
class TargetsViewModel @Inject constructor(
    private val repository: TargetsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TargetsUiState())
    val state: StateFlow<TargetsUiState> = _state

    private val _events = Channel<TargetsEvent>()
    val events = _events.receiveAsFlow()
    private var didPrefillFromSavedTarget = false

    init {
        viewModelScope.launch {
            repository.getTarget().collect { target ->
                if (target != null && !didPrefillFromSavedTarget) {
                    _state.update {
                        it.copy(
                            calories = target.calories.toString(),
                            fatG = target.fatG.toString(),
                            carbsG = target.carbsG.toString(),
                            proteinG = target.proteinG.toString(),
                        )
                    }
                    didPrefillFromSavedTarget = true
                }
            }
        }
    }

    fun onCaloriesChange(value: String) = _state.update { it.copy(calories = value) }
    fun onFatChange(value: String) = _state.update { it.copy(fatG = value) }
    fun onCarbsChange(value: String) = _state.update { it.copy(carbsG = value) }
    fun onProteinChange(value: String) = _state.update { it.copy(proteinG = value) }

    fun onSave(savedMsg: String, errorMsg: String) {
        val s = _state.value
        val calories = s.calories.toDoubleOrNull()
        val fat = s.fatG.toDoubleOrNull()
        val carbs = s.carbsG.toDoubleOrNull()
        val protein = s.proteinG.toDoubleOrNull()

        if (calories == null || fat == null || carbs == null || protein == null) {
            viewModelScope.launch { _events.send(TargetsEvent.ShowSnackbar(errorMsg)) }
            return
        }

        viewModelScope.launch {
            repository.saveTarget(
                DailyTarget(
                    id = 1,
                    calories = calories,
                    fatG = fat,
                    carbsG = carbs,
                    proteinG = protein,
                )
            )
            _events.send(TargetsEvent.ShowSnackbar(savedMsg))
            _events.send(TargetsEvent.NavigateToTrends)
        }
    }
}
