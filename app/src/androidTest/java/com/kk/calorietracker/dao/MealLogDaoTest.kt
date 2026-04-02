package com.kk.calorietracker.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.kk.calorietracker.data.db.CalorieTrackerDatabase
import com.kk.calorietracker.data.db.dao.FoodItemDao
import com.kk.calorietracker.data.db.dao.MealLogDao
import com.kk.calorietracker.data.db.dao.MealTypeDao
import com.kk.calorietracker.data.db.entity.FoodItemEntity
import com.kk.calorietracker.data.db.entity.MealLogEntity
import com.kk.calorietracker.data.db.entity.MealTypeEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MealLogDaoTest {

    private lateinit var db: CalorieTrackerDatabase
    private lateinit var mealLogDao: MealLogDao
    private lateinit var foodItemDao: FoodItemDao
    private lateinit var mealTypeDao: MealTypeDao

    private var foodItemId = 0L
    private var mealTypeId = 0L

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CalorieTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        mealLogDao = db.mealLogDao()
        foodItemDao = db.foodItemDao()
        mealTypeDao = db.mealTypeDao()

        foodItemId = foodItemDao.insertFoodItem(
            FoodItemEntity(name = "Chicken", measurementUnit = "PER_GRAM", measurementQuantity = 100.0, fatG = 3.6, carbsG = 0.0, proteinG = 31.0)
        )
        mealTypeId = mealTypeDao.insertMealType(MealTypeEntity(name = "Lunch"))
    }

    @After
    fun tearDown() = db.close()

    private fun log(epochDay: Long = LocalDate.now().toEpochDay()) = MealLogEntity(
        date = epochDay,
        mealTypeId = mealTypeId,
        foodItemId = foodItemId,
        quantity = 150.0,
        fatG = 5.4,
        carbsG = 0.0,
        proteinG = 46.5,
    )

    @Test
    fun insertAndGetLogsWithDetails() = runTest {
        val today = LocalDate.now().toEpochDay()
        mealLogDao.insertMealLog(log(today))
        mealLogDao.getLogsWithDetailsForDate(today).test {
            val logs = awaitItem()
            assertEquals(1, logs.size)
            assertEquals("Chicken", logs[0].foodItemName)
            assertEquals("Lunch", logs[0].mealTypeName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getMacroTotals_aggregatesCorrectly() = runTest {
        val today = LocalDate.now().toEpochDay()
        mealLogDao.insertMealLog(log(today))
        mealLogDao.insertMealLog(log(today))
        mealLogDao.getMacroTotalsForDate(today).test {
            val totals = awaitItem()
            assertEquals(10.8, totals.totalFatG, 0.001)
            assertEquals(93.0, totals.totalProteinG, 0.001)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDailyCaloriesForRange() = runTest {
        val today = LocalDate.now()
        mealLogDao.insertMealLog(log(today.toEpochDay()))
        mealLogDao.insertMealLog(log(today.minusDays(1).toEpochDay()))
        mealLogDao.getDailyCaloriesForRange(today.minusDays(6).toEpochDay(), today.toEpochDay()).test {
            val entries = awaitItem()
            assertEquals(2, entries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertMealLogs_insertsMultipleRows() = runTest {
        val today = LocalDate.now().toEpochDay()
        mealLogDao.insertMealLogs(
            listOf(
                log(today),
                log(today).copy(quantity = 50.0, fatG = 1.8, proteinG = 15.5),
            )
        )

        mealLogDao.getLogsWithDetailsForDate(today).test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteMealLog_removesEntry() = runTest {
        val today = LocalDate.now().toEpochDay()
        val id = mealLogDao.insertMealLog(log(today))
        mealLogDao.deleteMealLog(log(today).copy(id = id))
        mealLogDao.getLogsWithDetailsForDate(today).test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
