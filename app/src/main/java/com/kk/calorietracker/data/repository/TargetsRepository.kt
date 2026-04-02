package com.kk.calorietracker.data.repository

import com.kk.calorietracker.data.db.dao.DailyTargetDao
import com.kk.calorietracker.data.db.entity.DailyTargetEntity
import com.kk.calorietracker.data.model.DailyTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TargetsRepository @Inject constructor(
    private val dao: DailyTargetDao,
) {
    fun getTarget(): Flow<DailyTarget?> =
        dao.getTarget().map { it?.toDomain() }

    suspend fun saveTarget(target: DailyTarget) =
        dao.upsertTarget(DailyTargetEntity.fromDomain(target))
}
