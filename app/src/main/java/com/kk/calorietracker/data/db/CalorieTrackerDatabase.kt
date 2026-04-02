package com.kk.calorietracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kk.calorietracker.data.db.dao.DailyTargetDao
import com.kk.calorietracker.data.db.dao.FoodItemDao
import com.kk.calorietracker.data.db.dao.MealLogDao
import com.kk.calorietracker.data.db.dao.MealTypeDao
import com.kk.calorietracker.data.db.entity.DailyTargetEntity
import com.kk.calorietracker.data.db.entity.FoodItemEntity
import com.kk.calorietracker.data.db.entity.MealLogEntity
import com.kk.calorietracker.data.db.entity.MealTypeEntity

@Database(
    entities = [
        FoodItemEntity::class,
        MealTypeEntity::class,
        MealLogEntity::class,
        DailyTargetEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class CalorieTrackerDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun mealTypeDao(): MealTypeDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun dailyTargetDao(): DailyTargetDao
}
