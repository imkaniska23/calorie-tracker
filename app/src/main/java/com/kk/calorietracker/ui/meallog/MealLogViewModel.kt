package com.kk.calorietracker.ui.meallog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.calorietracker.data.db.dao.MealLogWithDetails
import com.kk.calorietracker.data.model.FoodItem
import com.kk.calorietracker.data.model.MealLog
import com.kk.calorietracker.data.model.MealType
import com.kk.calorietracker.data.repository.FoodRepository
import com.kk.calorietracker.data.repository.MealLogRepository
import com.kk.calorietracker.data.repository.MealTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MacroPreview(
    val fatG: Double = 0.0,
    val carbsG: Double = 0.0,
    val proteinG: Double = 0.0,
) {
    val calories: Double get() = (fatG * 9) + (carbsG * 4) + (proteinG * 4)
}

data class MealDraftItem(
    val foodItemId: Long,
    val foodItemName: String,
    val quantity: Double,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
)

data class MealLogUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMealType: MealType? = null,
    val selectedFoodItem: FoodItem? = null,
    val foodSearchQuery: String = "",
    val quantity: String = "",
    val macroPreview: MacroPreview = MacroPreview(),
    val mealDraftItems: List<MealDraftItem> = emptyList(),
    val mealDraftTotalPreview: MacroPreview = MacroPreview(),
    val mealTypes: List<MealType> = emptyList(),
    val filteredFoodItems: List<FoodItem> = emptyList(),
    val showDatePicker: Boolean = false,
)

sealed interface MealLogEvent {
    data class ShowSnackbar(val message: String) : MealLogEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MealLogViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val foodRepository: FoodRepository,
    private val mealTypeRepository: MealTypeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MealLogUiState())
    val state: StateFlow<MealLogUiState> = _state

    private val _events = Channel<MealLogEvent>()
    val events = _events.receiveAsFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val logsForDate: StateFlow<List<MealLogWithDetails>> = _selectedDate
        .flatMapLatest { date -> mealLogRepository.getLogsWithDetailsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            mealTypeRepository.getAllMealTypes().collect { types ->
                _state.update { it.copy(mealTypes = types) }
            }
        }
        viewModelScope.launch {
            foodRepository.searchFoodItems("").collect { items ->
                _state.update { it.copy(filteredFoodItems = items) }
            }
        }
    }

    fun onDateChange(date: LocalDate) {
        _selectedDate.value = date
        _state.update { it.copy(selectedDate = date, showDatePicker = false) }
    }

    fun onShowDatePicker() = _state.update { it.copy(showDatePicker = true) }
    fun onDismissDatePicker() = _state.update { it.copy(showDatePicker = false) }

    fun onMealTypeSelected(mealType: MealType) = _state.update { it.copy(selectedMealType = mealType) }

    fun onFoodSearchChange(query: String) {
        _state.update { it.copy(foodSearchQuery = query) }
        viewModelScope.launch {
            foodRepository.searchFoodItems(query).collect { items ->
                _state.update { it.copy(filteredFoodItems = items) }
            }
        }
    }

    fun onFoodItemSelected(item: FoodItem) {
        _state.update {
            it.copy(
                selectedFoodItem = item,
                foodSearchQuery = item.name,
            )
        }
        recalcMacros()
    }

    fun onQuantityChange(value: String) {
        _state.update { it.copy(quantity = value) }
        recalcMacros()
    }

    private fun recalcMacros() {
        val s = _state.value
        val food = s.selectedFoodItem ?: return
        val qty = s.quantity.toDoubleOrNull() ?: 0.0
        val ratio = if (food.measurementQuantity > 0) qty / food.measurementQuantity else 0.0
        _state.update {
            it.copy(
                macroPreview = MacroPreview(
                    fatG = food.fatG * ratio,
                    carbsG = food.carbsG * ratio,
                    proteinG = food.proteinG * ratio,
                )
            )
        }
    }

    private fun recalcDraftTotal() {
        val s = _state.value
        _state.update {
            it.copy(
                mealDraftTotalPreview = MacroPreview(
                    fatG = s.mealDraftItems.sumOf { item -> item.fatG },
                    carbsG = s.mealDraftItems.sumOf { item -> item.carbsG },
                    proteinG = s.mealDraftItems.sumOf { item -> item.proteinG },
                )
            )
        }
    }

    fun onAddItemToMeal(errorMsg: String) {
        val s = _state.value
        val food = s.selectedFoodItem
        val qty = s.quantity.toDoubleOrNull()

        if (food == null || qty == null || qty <= 0) {
            viewModelScope.launch { _events.send(MealLogEvent.ShowSnackbar(errorMsg)) }
            return
        }

        val ratio = qty / food.measurementQuantity
        val draftItem = MealDraftItem(
            foodItemId = food.id,
            foodItemName = food.name,
            quantity = qty,
            fatG = food.fatG * ratio,
            carbsG = food.carbsG * ratio,
            proteinG = food.proteinG * ratio,
        )

        _state.update {
            it.copy(
                mealDraftItems = it.mealDraftItems + draftItem,
                selectedFoodItem = null,
                foodSearchQuery = "",
                quantity = "",
                macroPreview = MacroPreview(),
            )
        }
        recalcDraftTotal()
    }

    fun onRemoveMealDraftItem(index: Int) {
        val s = _state.value
        if (index !in s.mealDraftItems.indices) return
        _state.update {
            it.copy(mealDraftItems = it.mealDraftItems.toMutableList().also { list -> list.removeAt(index) })
        }
        recalcDraftTotal()
    }

    fun onSave(savedMsg: String, errorMsg: String) {
        val s = _state.value
        val mealType = s.selectedMealType

        if (mealType == null || s.mealDraftItems.isEmpty()) {
            viewModelScope.launch { _events.send(MealLogEvent.ShowSnackbar(errorMsg)) }
            return
        }

        viewModelScope.launch {
            mealLogRepository.saveMealLogs(
                s.mealDraftItems.map { draftItem -> MealLog(
                    date = s.selectedDate,
                    mealTypeId = mealType.id,
                    foodItemId = draftItem.foodItemId,
                    quantity = draftItem.quantity,
                    fatG = draftItem.fatG,
                    carbsG = draftItem.carbsG,
                    proteinG = draftItem.proteinG,
                ) }
            )
            _state.update {
                it.copy(
                    selectedMealType = null,
                    selectedFoodItem = null,
                    foodSearchQuery = "",
                    quantity = "",
                    macroPreview = MacroPreview(),
                    mealDraftItems = emptyList(),
                    mealDraftTotalPreview = MacroPreview(),
                )
            }
            _events.send(MealLogEvent.ShowSnackbar(savedMsg))
        }
    }

    fun onDeleteLog(log: MealLogWithDetails, deletedMsg: String) {
        viewModelScope.launch {
            mealLogRepository.deleteMealLog(
                com.kk.calorietracker.data.model.MealLog(
                    id = log.id,
                    date = java.time.LocalDate.ofEpochDay(log.date),
                    mealTypeId = log.mealTypeId,
                    foodItemId = log.foodItemId,
                    quantity = log.quantity,
                    fatG = log.fatG,
                    carbsG = log.carbsG,
                    proteinG = log.proteinG,
                )
            )
            _events.send(MealLogEvent.ShowSnackbar(deletedMsg))
        }
    }
}
