package com.binod.mealmatefeb.data

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val preparationTime: Int // in minutes
)

@Serializable
data class Ingredient(
    val name: String,
    val quantity: Double,
    val unit: String
) 