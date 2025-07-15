package com.binod.mealmatefeb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking

private val Context.recipeDataStore: DataStore<Preferences> by preferencesDataStore(name = "recipes")

class RecipeRepository(
    private val context: Context,
    private val userRepository: UserRepository
) {
    private val defaultRecipes = listOf(
        Recipe(
            name = "Chicken Stir-Fry",
            ingredients = listOf(
                Ingredient("Chicken breast", 500.0, "g", IngredientCategory.MEAT_AND_POULTRY),
                Ingredient("Broccoli", 2.0, "cups", IngredientCategory.VEGETABLES),
                Ingredient("Carrots", 2.0, "pieces", IngredientCategory.VEGETABLES),
                Ingredient("Bell peppers", 2.0, "pieces", IngredientCategory.VEGETABLES),
                Ingredient("Soy sauce", 3.0, "tbsp", IngredientCategory.OILS_AND_VINEGARS),
                Ingredient("Ginger", 1.0, "tbsp", IngredientCategory.SPICES_AND_SEASONINGS),
                Ingredient("Garlic", 3.0, "cloves", IngredientCategory.SPICES_AND_SEASONINGS),
                Ingredient("Vegetable oil", 2.0, "tbsp", IngredientCategory.OILS_AND_VINEGARS)
            ),
            instructions = listOf(
                "Cut chicken into bite-sized pieces",
                "Chop all vegetables",
                "Heat oil in a large wok or skillet",
                "Cook chicken until golden brown",
                "Add vegetables and stir-fry until crisp-tender",
                "Add minced garlic and ginger",
                "Pour in soy sauce and stir well",
                "Cook for additional 2-3 minutes until everything is well combined"
            ),
            preparationTime = 30,
            imageUri = "android.resource://com.binod.mealmatefeb/drawable/chicken_stir_fry"
        ),
        Recipe(
            name = "Vegetable Quinoa Bowl",
            ingredients = listOf(
                Ingredient("Quinoa", 1.0, "cup", IngredientCategory.GRAINS_AND_PASTA),
                Ingredient("Sweet potato", 1.0, "piece", IngredientCategory.VEGETABLES),
                Ingredient("Chickpeas", 1.0, "can", IngredientCategory.CANNED_AND_JARRED),
                Ingredient("Kale", 2.0, "cups", IngredientCategory.VEGETABLES),
                Ingredient("Avocado", 1.0, "piece", IngredientCategory.VEGETABLES),
                Ingredient("Olive oil", 2.0, "tbsp", IngredientCategory.OILS_AND_VINEGARS),
                Ingredient("Lemon", 1.0, "piece", IngredientCategory.FRUITS),
                Ingredient("Cumin", 1.0, "tsp", IngredientCategory.SPICES_AND_SEASONINGS)
            ),
            instructions = listOf(
                "Cook quinoa according to package instructions",
                "Cube and roast sweet potato with olive oil and cumin",
                "Drain and rinse chickpeas",
                "Wash and chop kale",
                "Slice avocado",
                "Combine all ingredients in a bowl",
                "Drizzle with olive oil and lemon juice",
                "Season to taste"
            ),
            preparationTime = 35,
            imageUri = "android.resource://com.binod.mealmatefeb/drawable/quinoa_bowl"
        ),
        Recipe(
            name = "Spaghetti Carbonara",
            ingredients = listOf(
                Ingredient("Spaghetti", 400.0, "g", IngredientCategory.GRAINS_AND_PASTA),
                Ingredient("Eggs", 3.0, "pieces", IngredientCategory.DAIRY),
                Ingredient("Parmesan cheese", 100.0, "g", IngredientCategory.DAIRY),
                Ingredient("Pancetta", 150.0, "g", IngredientCategory.MEAT_AND_POULTRY),
                Ingredient("Black pepper", 1.0, "tsp", IngredientCategory.SPICES_AND_SEASONINGS),
                Ingredient("Salt", 1.0, "tsp", IngredientCategory.SPICES_AND_SEASONINGS),
                Ingredient("Garlic", 2.0, "cloves", IngredientCategory.SPICES_AND_SEASONINGS)
            ),
            instructions = listOf(
                "Bring a large pot of salted water to boil",
                "Cook spaghetti according to package instructions",
                "While pasta cooks, whisk eggs and grated parmesan in a bowl",
                "Dice pancetta and cook until crispy",
                "Add minced garlic to pancetta",
                "Reserve 1 cup of pasta water before draining",
                "Toss hot pasta with egg mixture and pancetta",
                "Add pasta water as needed for creamy consistency",
                "Season with black pepper and serve immediately"
            ),
            preparationTime = 25,
            imageUri = "android.resource://com.binod.mealmatefeb/drawable/spaghetti_carbonara"
        )
    )

    init {
        // Initialize default recipes when repository is created
        runBlocking {
            initializeDefaultRecipes()
        }
    }

    private suspend fun initializeDefaultRecipes() {
        val key = getCurrentUserRecipeKey()
        val preferences = context.recipeDataStore.data.first()
        val recipesString = preferences[key]
        if (recipesString == null || recipesString.isBlank()) {
            saveRecipes(defaultRecipes)
        }
    }

    private fun getUserRecipeKey(email: String): Preferences.Key<String> {
        return stringPreferencesKey("recipes_$email")
    }

    private suspend fun getCurrentUserRecipeKey(): Preferences.Key<String> {
        val currentUser = userRepository.getCurrentUser()
        return getUserRecipeKey(currentUser?.email ?: "default")
    }

    val recipes: Flow<List<Recipe>> = context.recipeDataStore.data
        .map { preferences ->
            val key = getCurrentUserRecipeKey()
            val recipesString = preferences[key] ?: ""
            try {
                if (recipesString.isBlank()) {
                    defaultRecipes
                } else {
                    Json.decodeFromString<List<Recipe>>(recipesString)
                }
            } catch (e: Exception) {
                defaultRecipes
            }
        }
        .onEach { recipeList ->
            if (recipeList.isEmpty()) {
                saveRecipes(defaultRecipes)
            }
        }

    suspend fun saveRecipes(recipes: List<Recipe>) {
        val key = getCurrentUserRecipeKey()
        context.recipeDataStore.edit { preferences ->
            preferences[key] = Json.encodeToString(recipes)
        }
    }

    suspend fun clearRecipes() {
        val key = getCurrentUserRecipeKey()
        context.recipeDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
} 