package com.kk.calorietracker.repository

import app.cash.turbine.test
import com.kk.calorietracker.data.db.dao.FoodItemDao
import com.kk.calorietracker.data.db.entity.FoodItemEntity
import com.kk.calorietracker.data.model.FoodItem
import com.kk.calorietracker.data.model.MeasurementUnit
import com.kk.calorietracker.data.repository.FoodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodRepositoryTest {

    private val dao = mockk<FoodItemDao>()
    private val repository = FoodRepository(dao)

    private val entity = FoodItemEntity(
        id = 1,
        name = "Apple",
        measurementUnit = "PER_GRAM",
        measurementQuantity = 100.0,
        fatG = 0.2,
        carbsG = 13.8,
        proteinG = 0.3,
    )

    private val domain = FoodItem(
        id = 1,
        name = "Apple",
        measurementUnit = MeasurementUnit.PER_GRAM,
        measurementQuantity = 100.0,
        fatG = 0.2,
        carbsG = 13.8,
        proteinG = 0.3,
    )

    @Test
    fun `getAllFoodItems maps entities to domain models`() = runTest {
        every { dao.getAllFoodItems() } returns flowOf(listOf(entity))

        repository.getAllFoodItems().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(domain, items.first())
            awaitComplete()
        }
    }

    @Test
    fun `saveFoodItem inserts when id is zero`() = runTest {
        val newFood = domain.copy(id = 0)
        coEvery { dao.insertFoodItem(any()) } returns 1L

        val id = repository.saveFoodItem(newFood)

        assertEquals(1L, id)
        coVerify { dao.insertFoodItem(FoodItemEntity.fromDomain(newFood)) }
    }

    @Test
    fun `saveFoodItem updates when id exists`() = runTest {
        coEvery { dao.updateFoodItem(any()) } returns Unit

        val id = repository.saveFoodItem(domain)

        assertEquals(domain.id, id)
        coVerify { dao.updateFoodItem(FoodItemEntity.fromDomain(domain)) }
    }

    @Test
    fun `deleteFoodItem calls delete`() = runTest {
        coEvery { dao.deleteFoodItem(any()) } returns Unit

        repository.deleteFoodItem(domain)

        coVerify { dao.deleteFoodItem(FoodItemEntity.fromDomain(domain)) }
    }

    @Test
    fun `getFoodItemById returns null when not found`() = runTest {
        coEvery { dao.getFoodItemById(99) } returns null

        val result = repository.getFoodItemById(99)

        assertEquals(null, result)
    }

    @Test
    fun `getFoodItemById maps entity to domain`() = runTest {
        coEvery { dao.getFoodItemById(1) } returns entity

        val result = repository.getFoodItemById(1)

        assertEquals(domain, result)
    }
}
