package com.binod.mealmatefeb.data

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val quantity: Int,
    val isBought: Boolean = false,
    val category: IngredientCategory = IngredientCategory.OTHER
) 