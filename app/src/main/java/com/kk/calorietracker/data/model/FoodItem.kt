package com.kk.calorietracker.data.model

data class FoodItem(
    val id: Long = 0,
    val name: String,
    val measurementUnit: MeasurementUnit,
    val measurementQuantity: Double,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
)
