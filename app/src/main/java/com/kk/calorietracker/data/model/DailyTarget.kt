package com.kk.calorietracker.data.model

data class DailyTarget(
    val id: Long = 1,
    val calories: Double,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
)
