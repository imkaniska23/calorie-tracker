package com.kk.calorietracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kk.calorietracker.data.model.DailyTarget

@Entity(tableName = "daily_target")
data class DailyTargetEntity(
    @PrimaryKey val id: Long = 1,
    val calories: Double,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
) {
    fun toDomain() = DailyTarget(
        id = id,
        calories = calories,
        fatG = fatG,
        carbsG = carbsG,
        proteinG = proteinG,
    )

    companion object {
        fun fromDomain(domain: DailyTarget) = DailyTargetEntity(
            id = domain.id,
            calories = domain.calories,
            fatG = domain.fatG,
            carbsG = domain.carbsG,
            proteinG = domain.proteinG,
        )
    }
}
