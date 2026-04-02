package com.kk.calorietracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kk.calorietracker.data.model.MealType

@Entity(tableName = "meal_type")
data class MealTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
) {
    fun toDomain() = MealType(id = id, name = name)

    companion object {
        fun fromDomain(domain: MealType) = MealTypeEntity(id = domain.id, name = domain.name)
    }
}
