package com.kk.calorietracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kk.calorietracker.data.model.MealLog
import java.time.LocalDate

@Entity(
    tableName = "meal_log",
    foreignKeys = [
        ForeignKey(
            entity = MealTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealTypeId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("mealTypeId"),
        Index("foodItemId"),
        Index("date"),
    ],
)
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val mealTypeId: Long,
    val foodItemId: Long,
    val quantity: Double,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
) {
    fun toDomain() = MealLog(
        id = id,
        date = LocalDate.ofEpochDay(date),
        mealTypeId = mealTypeId,
        foodItemId = foodItemId,
        quantity = quantity,
        fatG = fatG,
        carbsG = carbsG,
        proteinG = proteinG,
    )

    companion object {
        fun fromDomain(domain: MealLog) = MealLogEntity(
            id = domain.id,
            date = domain.date.toEpochDay(),
            mealTypeId = domain.mealTypeId,
            foodItemId = domain.foodItemId,
            quantity = domain.quantity,
            fatG = domain.fatG,
            carbsG = domain.carbsG,
            proteinG = domain.proteinG,
        )
    }
}
