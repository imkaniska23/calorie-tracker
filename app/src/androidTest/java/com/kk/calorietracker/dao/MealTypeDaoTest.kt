package com.kk.calorietracker.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.kk.calorietracker.data.db.CalorieTrackerDatabase
import com.kk.calorietracker.data.db.dao.MealTypeDao
import com.kk.calorietracker.data.db.entity.MealTypeEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealTypeDaoTest {

    private lateinit var db: CalorieTrackerDatabase
    private lateinit var dao: MealTypeDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CalorieTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.mealTypeDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertAndGetAll() = runTest {
        dao.insertMealType(MealTypeEntity(name = "Breakfast"))
        dao.insertMealType(MealTypeEntity(name = "Lunch"))
        dao.getAllMealTypes().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun delete_removesItem() = runTest {
        val id = dao.insertMealType(MealTypeEntity(name = "Breakfast"))
        val item = dao.getMealTypeById(id)!!
        dao.deleteMealType(item)
        assertNull(dao.getMealTypeById(id))
    }

    @Test
    fun getById_returnsCorrect() = runTest {
        val id = dao.insertMealType(MealTypeEntity(name = "Dinner"))
        val item = dao.getMealTypeById(id)
        assertEquals("Dinner", item?.name)
    }
}
