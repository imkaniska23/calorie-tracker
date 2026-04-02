package com.kk.calorietracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kk.calorietracker.data.model.FoodItem
import com.kk.calorietracker.data.model.MeasurementUnit

@Entity(tableName = "food_item")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val measurementUnit: String,
    val measurementQuantity: Double,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
) {
    fun toDomain() = FoodItem(
        id = id,
        name = name,
        measurementUnit = MeasurementUnit.valueOf(measurementUnit),
        measurementQuantity = measurementQuantity,
        fatG = fatG,
        carbsG = carbsG,
        proteinG = proteinG,
    )

    companion object {
        fun fromDomain(domain: FoodItem) = FoodItemEntity(
            id = domain.id,
            name = domain.name,
            measurementUnit = domain.measurementUnit.name,
            measurementQuantity = domain.measurementQuantity,
            fatG = domain.fatG,
            carbsG = domain.carbsG,
            proteinG = domain.proteinG,
        )
    }
}
