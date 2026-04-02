package com.kk.calorietracker.repository

import app.cash.turbine.test
import com.kk.calorietracker.data.db.dao.DailyCaloriesResult
import com.kk.calorietracker.data.db.dao.MacroTotalsResult
import com.kk.calorietracker.data.db.dao.MealLogDao
import com.kk.calorietracker.data.db.dao.MealTypeCaloriesResult
import com.kk.calorietracker.data.db.entity.MealLogEntity
import com.kk.calorietracker.data.model.MealLog
import com.kk.calorietracker.data.repository.MealLogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MealLogRepositoryTest {

    private val dao = mockk<MealLogDao>()
    private val repository = MealLogRepository(dao)

    private val today = LocalDate.of(2026, 4, 2)

    @Test
    fun `getMealTypeCaloriesForDate delegates to dao`() = runTest {
        val result = listOf(MealTypeCaloriesResult("Lunch", 500.0))
        every { dao.getMealTypeCaloriesForDate(today.toEpochDay()) } returns flowOf(result)

        repository.getMealTypeCaloriesForDate(today).test {
            assertEquals(result, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getMacroTotalsForDate delegates to dao`() = runTest {
        val result = MacroTotalsResult(10.0, 50.0, 30.0)
        every { dao.getMacroTotalsForDate(today.toEpochDay()) } returns flowOf(result)

        repository.getMacroTotalsForDate(today).test {
            assertEquals(result, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getDailyCaloriesForRange delegates to dao`() = runTest {
        val start = today.minusDays(6)
        val result = listOf(DailyCaloriesResult(today.toEpochDay(), 1800.0))
        every { dao.getDailyCaloriesForRange(start.toEpochDay(), today.toEpochDay()) } returns flowOf(result)

        repository.getDailyCaloriesForRange(start, today).test {
            assertEquals(result, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `saveMealLog inserts and returns id`() = runTest {
        val mealLog = MealLog(
            date = today,
            mealTypeId = 1,
            foodItemId = 1,
            quantity = 150.0,
            fatG = 5.0,
            carbsG = 0.0,
            proteinG = 30.0,
        )
        coEvery { dao.insertMealLog(any()) } returns 42L

        val id = repository.saveMealLog(mealLog)

        assertEquals(42L, id)
        coVerify { dao.insertMealLog(MealLogEntity.fromDomain(mealLog)) }
    }

    @Test
    fun `saveMealLogs inserts all and returns ids`() = runTest {
        val mealLogs = listOf(
            MealLog(
                date = today,
                mealTypeId = 1,
                foodItemId = 1,
                quantity = 150.0,
                fatG = 5.0,
                carbsG = 0.0,
                proteinG = 30.0,
            ),
            MealLog(
                date = today,
                mealTypeId = 1,
                foodItemId = 2,
                quantity = 200.0,
                fatG = 1.0,
                carbsG = 10.0,
                proteinG = 12.0,
            ),
        )
        coEvery { dao.insertMealLogs(any()) } returns listOf(101L, 102L)

        val ids = repository.saveMealLogs(mealLogs)

        assertEquals(listOf(101L, 102L), ids)
        coVerify { dao.insertMealLogs(mealLogs.map { MealLogEntity.fromDomain(it) }) }
    }
}
