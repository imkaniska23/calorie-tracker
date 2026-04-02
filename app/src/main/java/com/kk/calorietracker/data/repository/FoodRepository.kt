package com.kk.calorietracker.data.repository

import com.kk.calorietracker.data.db.dao.FoodItemDao
import com.kk.calorietracker.data.db.entity.FoodItemEntity
import com.kk.calorietracker.data.model.FoodItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val dao: FoodItemDao,
) {
    fun getAllFoodItems(): Flow<List<FoodItem>> =
        dao.getAllFoodItems().map { list -> list.map { it.toDomain() } }

    fun searchFoodItems(query: String): Flow<List<FoodItem>> =
        dao.searchFoodItems(query).map { list -> list.map { it.toDomain() } }

    suspend fun getFoodItemById(id: Long): FoodItem? =
        dao.getFoodItemById(id)?.toDomain()

    suspend fun saveFoodItem(foodItem: FoodItem): Long {
        val entity = FoodItemEntity.fromDomain(foodItem)
        return if (foodItem.id == 0L) {
            dao.insertFoodItem(entity)
        } else {
            dao.updateFoodItem(entity)
            foodItem.id
        }
    }

    suspend fun deleteFoodItem(foodItem: FoodItem) =
        dao.deleteFoodItem(FoodItemEntity.fromDomain(foodItem))
}
