package com.kk.calorietracker.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kk.calorietracker.data.model.DailyTarget
import com.kk.calorietracker.data.repository.MealLogRepository
import com.kk.calorietracker.data.repository.TargetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

enum class TrendsTab { DAY, WEEK, MONTH }

data class MealTypeCaloriesEntry(val mealTypeName: String, val calories: Double)
data class DailyCaloriesEntry(val date: LocalDate, val calories: Double)
data class MacroTotals(val fatG: Double, val carbsG: Double, val proteinG: Double) {
    val calories: Double get() = (fatG * 9) + (carbsG * 4) + (proteinG * 4)
}

data class TrendsUiState(
    val selectedTab: TrendsTab = TrendsTab.DAY,
    val selectedDate: LocalDate = LocalDate.now(),
    val mealTypeCalories: List<MealTypeCaloriesEntry> = emptyList(),
    val macroTotals: MacroTotals = MacroTotals(0.0, 0.0, 0.0),
    val weeklyCalories: List<DailyCaloriesEntry> = emptyList(),
    val monthlyCalories: List<DailyCaloriesEntry> = emptyList(),
    val dailyTarget: DailyTarget? = null,
)

private data class DayFlows(
    val tab: TrendsTab,
    val date: LocalDate,
    val mealTypeCalories: List<MealTypeCaloriesEntry>,
    val macroTotals: MacroTotals,
    val target: DailyTarget?,
)

private data class RangeFlows(
    val weeklyCalories: List<DailyCaloriesEntry>,
    val monthlyCalories: List<DailyCaloriesEntry>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val targetsRepository: TargetsRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(TrendsTab.DAY)
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val mealTypeCaloriesFlow = _selectedDate.flatMapLatest { date ->
        mealLogRepository.getMealTypeCaloriesForDate(date)
    }

    private val macroTotalsFlow = _selectedDate.flatMapLatest { date ->
        mealLogRepository.getMacroTotalsForDate(date)
    }

    private val weeklyCaloriesFlow = _selectedDate.flatMapLatest { date ->
        mealLogRepository.getDailyCaloriesForRange(date.minusDays(6), date)
    }

    private val monthlyCaloriesFlow = _selectedDate.flatMapLatest { date ->
        mealLogRepository.getDailyCaloriesForRange(date.minusDays(29), date)
    }

    private val dayFlows = combine(
        _selectedTab,
        _selectedDate,
        mealTypeCaloriesFlow,
        macroTotalsFlow,
        targetsRepository.getTarget(),
    ) { tab, date, mealTypeCals, macros, target ->
        DayFlows(
            tab = tab,
            date = date,
            mealTypeCalories = mealTypeCals.map { MealTypeCaloriesEntry(it.mealTypeName, it.totalCalories) },
            macroTotals = MacroTotals(macros.totalFatG, macros.totalCarbsG, macros.totalProteinG),
            target = target,
        )
    }

    private val rangeFlows = combine(weeklyCaloriesFlow, monthlyCaloriesFlow) { weekly, monthly ->
        RangeFlows(
            weeklyCalories = weekly.map { DailyCaloriesEntry(LocalDate.ofEpochDay(it.date), it.totalCalories) },
            monthlyCalories = monthly.map { DailyCaloriesEntry(LocalDate.ofEpochDay(it.date), it.totalCalories) },
        )
    }

    val state: StateFlow<TrendsUiState> = combine(dayFlows, rangeFlows) { day, range ->
        TrendsUiState(
            selectedTab = day.tab,
            selectedDate = day.date,
            mealTypeCalories = day.mealTypeCalories,
            macroTotals = day.macroTotals,
            weeklyCalories = range.weeklyCalories,
            monthlyCalories = range.monthlyCalories,
            dailyTarget = day.target,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrendsUiState())

    fun onTabSelected(tab: TrendsTab) = _selectedTab.update { tab }
    fun onDateSelected(date: LocalDate) = _selectedDate.update { date }
}
