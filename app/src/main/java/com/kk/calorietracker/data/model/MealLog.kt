package com.kk.calorietracker.data.model

import java.time.LocalDate

data class MealLog(
    val id: Long = 0,
    val date: LocalDate,
    val mealTypeId: Long,
    val foodItemId: Long,
    val quantity: Double,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
)
