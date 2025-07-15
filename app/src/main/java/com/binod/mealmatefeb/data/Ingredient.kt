package com.binod.mealmatefeb.data

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: IngredientCategory = IngredientCategory.OTHER
)

enum class IngredientCategory {
    DAIRY,
    MEAT_AND_POULTRY,
    FISH_AND_SEAFOOD,
    VEGETABLES,
    FRUITS,
    GRAINS_AND_PASTA,
    SPICES_AND_SEASONINGS,
    OILS_AND_VINEGARS,
    BAKING,
    CANNED_AND_JARRED,
    OTHER;

    fun displayName(): String {
        return when (this) {
            DAIRY -> "Dairy"
            MEAT_AND_POULTRY -> "Meat & Poultry"
            FISH_AND_SEAFOOD -> "Fish & Seafood"
            VEGETABLES -> "Vegetables"
            FRUITS -> "Fruits"
            GRAINS_AND_PASTA -> "Grains & Pasta"
            SPICES_AND_SEASONINGS -> "Spices & Seasonings"
            OILS_AND_VINEGARS -> "Oils & Vinegars"
            BAKING -> "Baking"
            CANNED_AND_JARRED -> "Canned & Jarred"
            OTHER -> "Other"
        }
    }
} 