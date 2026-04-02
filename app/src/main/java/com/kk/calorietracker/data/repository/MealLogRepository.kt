package com.kk.calorietracker.data.repository

import com.kk.calorietracker.data.db.dao.DailyCaloriesResult
import com.kk.calorietracker.data.db.dao.MacroTotalsResult
import com.kk.calorietracker.data.db.dao.MealLogDao
import com.kk.calorietracker.data.db.dao.MealLogWithDetails
import com.kk.calorietracker.data.db.dao.MealTypeCaloriesResult
import com.kk.calorietracker.data.db.entity.MealLogEntity
import com.kk.calorietracker.data.model.MealLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealLogRepository @Inject constructor(
    private val dao: MealLogDao,
) {
    fun getLogsWithDetailsForDate(date: LocalDate): Flow<List<MealLogWithDetails>> =
        dao.getLogsWithDetailsForDate(date.toEpochDay())

    fun getMealTypeCaloriesForDate(date: LocalDate): Flow<List<MealTypeCaloriesResult>> =
        dao.getMealTypeCaloriesForDate(date.toEpochDay())

    fun getMacroTotalsForDate(date: LocalDate): Flow<MacroTotalsResult> =
        dao.getMacroTotalsForDate(date.toEpochDay())

    fun getDailyCaloriesForRange(start: LocalDate, end: LocalDate): Flow<List<DailyCaloriesResult>> =
        dao.getDailyCaloriesForRange(start.toEpochDay(), end.toEpochDay())

    suspend fun saveMealLog(mealLog: MealLog): Long =
        dao.insertMealLog(MealLogEntity.fromDomain(mealLog))

    suspend fun saveMealLogs(mealLogs: List<MealLog>): List<Long> =
        dao.insertMealLogs(mealLogs.map { mealLog -> MealLogEntity.fromDomain(mealLog) })

    suspend fun deleteMealLog(mealLog: MealLog) =
        dao.deleteMealLog(MealLogEntity.fromDomain(mealLog))
}
