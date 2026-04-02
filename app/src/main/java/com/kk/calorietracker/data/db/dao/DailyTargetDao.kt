package com.kk.calorietracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kk.calorietracker.data.db.entity.DailyTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTargetDao {

    @Query("SELECT * FROM daily_target WHERE id = 1")
    fun getTarget(): Flow<DailyTargetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTarget(entity: DailyTargetEntity)
}
