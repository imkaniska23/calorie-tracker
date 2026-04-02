package com.kk.calorietracker.data.db.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kk.calorietracker.data.db.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

data class MealLogWithDetails(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "mealTypeId") val mealTypeId: Long,
    @ColumnInfo(name = "foodItemId") val foodItemId: Long,
    @ColumnInfo(name = "quantity") val quantity: Double,
    @ColumnInfo(name = "fatG") val fatG: Double,
    @ColumnInfo(name = "carbsG") val carbsG: Double,
    @ColumnInfo(name = "proteinG") val proteinG: Double,
    @ColumnInfo(name = "mealTypeName") val mealTypeName: String,
    @ColumnInfo(name = "foodItemName") val foodItemName: String,
)

data class MealTypeCaloriesResult(
    @ColumnInfo(name = "mealTypeName") val mealTypeName: String,
    @ColumnInfo(name = "totalCalories") val totalCalories: Double,
)

data class DailyCaloriesResult(
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "totalCalories") val totalCalories: Double,
)

data class MacroTotalsResult(
    @ColumnInfo(name = "totalFatG") val totalFatG: Double,
    @ColumnInfo(name = "totalCarbsG") val totalCarbsG: Double,
    @ColumnInfo(name = "totalProteinG") val totalProteinG: Double,
)

@Dao
interface MealLogDao {

    @Query("""
        SELECT ml.id, ml.date, ml.mealTypeId, ml.foodItemId, ml.quantity,
               ml.fatG, ml.carbsG, ml.proteinG,
               mt.name AS mealTypeName, fi.name AS foodItemName
        FROM meal_log ml
        INNER JOIN meal_type mt ON ml.mealTypeId = mt.id
        INNER JOIN food_item fi ON ml.foodItemId = fi.id
        WHERE ml.date = :epochDay
        ORDER BY ml.id DESC
    """)
    fun getLogsWithDetailsForDate(epochDay: Long): Flow<List<MealLogWithDetails>>

    @Query("""
        SELECT mt.name AS mealTypeName,
               SUM(ml.fatG * 9 + ml.carbsG * 4 + ml.proteinG * 4) AS totalCalories
        FROM meal_log ml
        INNER JOIN meal_type mt ON ml.mealTypeId = mt.id
        WHERE ml.date = :epochDay
        GROUP BY ml.mealTypeId
        ORDER BY totalCalories DESC
    """)
    fun getMealTypeCaloriesForDate(epochDay: Long): Flow<List<MealTypeCaloriesResult>>

    @Query("""
        SELECT COALESCE(SUM(fatG), 0.0) AS totalFatG,
               COALESCE(SUM(carbsG), 0.0) AS totalCarbsG,
               COALESCE(SUM(proteinG), 0.0) AS totalProteinG
        FROM meal_log
        WHERE date = :epochDay
    """)
    fun getMacroTotalsForDate(epochDay: Long): Flow<MacroTotalsResult>

    @Query("""
        SELECT date,
               SUM(fatG * 9 + carbsG * 4 + proteinG * 4) AS totalCalories
        FROM meal_log
        WHERE date >= :startEpochDay AND date <= :endEpochDay
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyCaloriesForRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyCaloriesResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(entity: MealLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLogs(entities: List<MealLogEntity>): List<Long>

    @Delete
    suspend fun deleteMealLog(entity: MealLogEntity)
}
