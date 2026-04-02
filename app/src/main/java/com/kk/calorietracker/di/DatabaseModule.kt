package com.kk.calorietracker.di

import android.content.Context
import androidx.room.Room
import com.kk.calorietracker.data.db.CalorieTrackerDatabase
import com.kk.calorietracker.data.db.dao.DailyTargetDao
import com.kk.calorietracker.data.db.dao.FoodItemDao
import com.kk.calorietracker.data.db.dao.MealLogDao
import com.kk.calorietracker.data.db.dao.MealTypeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CalorieTrackerDatabase =
        Room.databaseBuilder(
            context,
            CalorieTrackerDatabase::class.java,
            "calorie_tracker.db",
        ).build()

    @Provides
    fun provideFoodItemDao(db: CalorieTrackerDatabase): FoodItemDao = db.foodItemDao()

    @Provides
    fun provideMealTypeDao(db: CalorieTrackerDatabase): MealTypeDao = db.mealTypeDao()

    @Provides
    fun provideMealLogDao(db: CalorieTrackerDatabase): MealLogDao = db.mealLogDao()

    @Provides
    fun provideDailyTargetDao(db: CalorieTrackerDatabase): DailyTargetDao = db.dailyTargetDao()
}
