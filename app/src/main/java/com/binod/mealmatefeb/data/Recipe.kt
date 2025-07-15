package com.binod.mealmatefeb.data

import kotlinx.serialization.Serializable
import com.binod.mealmatefeb.data.Ingredient

@Serializable
data class Recipe(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val preparationTime: Int, // in minutes
    val imageUri: String? = null // URI string for the recipe image
) 