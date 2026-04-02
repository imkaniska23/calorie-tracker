package com.kk.calorietracker.data.repository

import com.kk.calorietracker.data.db.dao.MealTypeDao
import com.kk.calorietracker.data.db.entity.MealTypeEntity
import com.kk.calorietracker.data.model.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealTypeRepository @Inject constructor(
    private val dao: MealTypeDao,
) {
    fun getAllMealTypes(): Flow<List<MealType>> =
        dao.getAllMealTypes().map { list -> list.map { it.toDomain() } }

    suspend fun getMealTypeById(id: Long): MealType? =
        dao.getMealTypeById(id)?.toDomain()

    suspend fun saveMealType(mealType: MealType): Long =
        dao.insertMealType(MealTypeEntity.fromDomain(mealType))

    suspend fun deleteMealType(mealType: MealType) =
        dao.deleteMealType(MealTypeEntity.fromDomain(mealType))
}
