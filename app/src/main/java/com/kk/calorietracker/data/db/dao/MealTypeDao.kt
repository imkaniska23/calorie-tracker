package com.kk.calorietracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kk.calorietracker.data.db.entity.MealTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTypeDao {

    @Query("SELECT * FROM meal_type ORDER BY name ASC")
    fun getAllMealTypes(): Flow<List<MealTypeEntity>>

    @Query("SELECT DISTINCT mealTypeId FROM meal_log")
    fun getReferencedMealTypeIds(): Flow<List<Long>>

    @Query("SELECT * FROM meal_type WHERE id = :id")
    suspend fun getMealTypeById(id: Long): MealTypeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealType(entity: MealTypeEntity): Long

    @Delete
    suspend fun deleteMealType(entity: MealTypeEntity)
}
