package com.kk.calorietracker.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.kk.calorietracker.data.db.CalorieTrackerDatabase
import com.kk.calorietracker.data.db.dao.DailyTargetDao
import com.kk.calorietracker.data.db.entity.DailyTargetEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyTargetDaoTest {

    private lateinit var db: CalorieTrackerDatabase
    private lateinit var dao: DailyTargetDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CalorieTrackerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dailyTargetDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun getTarget_returnsNullInitially() = runTest {
        dao.getTarget().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun upsert_insertsThenUpdates() = runTest {
        val first = DailyTargetEntity(id = 1, calories = 2000.0, fatG = 67.0, carbsG = 250.0, proteinG = 100.0)
        dao.upsertTarget(first)

        dao.getTarget().test {
            assertEquals(2000.0, awaitItem()?.calories)
            cancelAndIgnoreRemainingEvents()
        }

        val updated = first.copy(calories = 1800.0)
        dao.upsertTarget(updated)

        dao.getTarget().test {
            assertEquals(1800.0, awaitItem()?.calories)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
