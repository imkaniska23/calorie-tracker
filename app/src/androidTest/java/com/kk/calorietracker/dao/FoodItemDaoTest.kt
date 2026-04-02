package com.kk.calorietracker.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.kk.calorietracker.data.db.CalorieTrackerDatabase
import com.kk.calorietracker.data.db.dao.FoodItemDao
import com.kk.calorietracker.data.db.entity.FoodItemEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodItemDaoTest {

    private lateinit var db: CalorieTrackerDatabase
    private lateinit var dao: FoodItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CalorieTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.foodItemDao()
    }

    @After
    fun tearDown() = db.close()

    private fun foodItem(name: String = "Apple") = FoodItemEntity(
        name = name,
        measurementUnit = "PER_GRAM",
        measurementQuantity = 100.0,
        fatG = 0.2,
        carbsG = 13.8,
        proteinG = 0.3,
    )

    @Test
    fun insertAndGetAll() = runTest {
        dao.insertFoodItem(foodItem("Apple"))
        dao.insertFoodItem(foodItem("Banana"))
        dao.getAllFoodItems().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getById_returnsCorrectItem() = runTest {
        val id = dao.insertFoodItem(foodItem("Apple"))
        val item = dao.getFoodItemById(id)
        assertNotNull(item)
        assertEquals("Apple", item!!.name)
    }

    @Test
    fun delete_removesItem() = runTest {
        val id = dao.insertFoodItem(foodItem("Apple"))
        val item = dao.getFoodItemById(id)!!
        dao.deleteFoodItem(item)
        assertNull(dao.getFoodItemById(id))
    }

    @Test
    fun search_returnsMatchingItems() = runTest {
        dao.insertFoodItem(foodItem("Apple"))
        dao.insertFoodItem(foodItem("Apricot"))
        dao.insertFoodItem(foodItem("Banana"))
        dao.searchFoodItems("Ap").test {
            val items = awaitItem()
            assertEquals(2, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun update_changesItem() = runTest {
        val id = dao.insertFoodItem(foodItem("Apple"))
        val item = dao.getFoodItemById(id)!!
        dao.updateFoodItem(item.copy(name = "Green Apple"))
        assertEquals("Green Apple", dao.getFoodItemById(id)?.name)
    }
}
