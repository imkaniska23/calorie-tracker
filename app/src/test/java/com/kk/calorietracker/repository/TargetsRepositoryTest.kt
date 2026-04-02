package com.kk.calorietracker.repository

import app.cash.turbine.test
import com.kk.calorietracker.data.db.dao.DailyTargetDao
import com.kk.calorietracker.data.db.entity.DailyTargetEntity
import com.kk.calorietracker.data.model.DailyTarget
import com.kk.calorietracker.data.repository.TargetsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetsRepositoryTest {

    private val dao = mockk<DailyTargetDao>()
    private val repository = TargetsRepository(dao)

    private val entity = DailyTargetEntity(id = 1, calories = 2000.0, fatG = 67.0, carbsG = 250.0, proteinG = 100.0)
    private val domain = DailyTarget(id = 1, calories = 2000.0, fatG = 67.0, carbsG = 250.0, proteinG = 100.0)

    @Test
    fun `getTarget emits null when no target set`() = runTest {
        every { dao.getTarget() } returns flowOf(null)

        repository.getTarget().test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getTarget maps entity to domain`() = runTest {
        every { dao.getTarget() } returns flowOf(entity)

        repository.getTarget().test {
            assertEquals(domain, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `saveTarget calls upsert with correct entity`() = runTest {
        coEvery { dao.upsertTarget(any()) } returns Unit

        repository.saveTarget(domain)

        coVerify { dao.upsertTarget(DailyTargetEntity.fromDomain(domain)) }
    }
}
