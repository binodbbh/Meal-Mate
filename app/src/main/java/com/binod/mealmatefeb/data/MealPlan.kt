package com.binod.mealmatefeb.data

import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(
    val id: String = java.util.UUID.randomUUID().toString(),
    val weekStartDate: Long, // Unix timestamp for the start of the week
    val dailyPlans: Map<Int, DayPlan> // Key: Day of week (1-7), Value: DayPlan
)

@Serializable
data class DayPlan(
    val breakfast: Recipe? = null,
    val lunch: Recipe? = null,
    val dinner: Recipe? = null
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER
} 