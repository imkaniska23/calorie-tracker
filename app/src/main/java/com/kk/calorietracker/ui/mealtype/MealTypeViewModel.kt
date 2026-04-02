package com.kk.calorietracker.ui.mealtype

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.calorietracker.data.model.MealType
import com.kk.calorietracker.data.repository.MealTypeRepository
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

data class MealTypeUiState(
    val name: String = "",
    val showDeleteConfirm: MealType? = null,
    val referencedMealTypeIds: Set<Long> = emptySet(),
)

sealed interface MealTypeEvent {
    data class ShowSnackbar(val message: String) : MealTypeEvent
}

@HiltViewModel
class MealTypeViewModel @Inject constructor(
    private val repository: MealTypeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MealTypeUiState())
    val state: StateFlow<MealTypeUiState> = _state

    private val _events = Channel<MealTypeEvent>()
    val events = _events.receiveAsFlow()

    val items: StateFlow<List<MealType>> = repository.getAllMealTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.getReferencedMealTypeIds().collect { referencedIds ->
                _state.update { it.copy(referencedMealTypeIds = referencedIds) }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }

    fun onDeleteRequest(item: MealType) = _state.update { it.copy(showDeleteConfirm = item) }
    fun onDeleteDismiss() = _state.update { it.copy(showDeleteConfirm = null) }

    fun onDeleteConfirm(item: MealType, successMsg: String, blockedMsg: String, failedMsg: String) {
        _state.update { it.copy(showDeleteConfirm = null) }
        viewModelScope.launch {
            try {
                repository.deleteMealType(item)
                _events.send(MealTypeEvent.ShowSnackbar(successMsg))
            } catch (_: SQLiteConstraintException) {
                _events.send(MealTypeEvent.ShowSnackbar(blockedMsg))
            } catch (_: Exception) {
                _events.send(MealTypeEvent.ShowSnackbar(failedMsg))
            }
        }
    }

    fun onSave(savedMsg: String, errorMsg: String) {
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            viewModelScope.launch { _events.send(MealTypeEvent.ShowSnackbar(errorMsg)) }
            return
        }
        viewModelScope.launch {
            repository.saveMealType(MealType(name = name))
            _state.update { it.copy(name = "") }
            _events.send(MealTypeEvent.ShowSnackbar(savedMsg))
        }
    }
}
