package com.binod.mealmatefeb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.recipeDataStore: DataStore<Preferences> by preferencesDataStore(name = "recipes")

class RecipeRepository(private val context: Context) {
    private object PreferencesKeys {
        val RECIPES = stringPreferencesKey("recipes")
    }

    val recipes: Flow<List<Recipe>> = context.recipeDataStore.data.map { preferences ->
        val recipesString = preferences[PreferencesKeys.RECIPES] ?: "[]"
        try {
            Json.decodeFromString<List<Recipe>>(recipesString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveRecipes(recipes: List<Recipe>) {
        context.recipeDataStore.edit { preferences ->
            preferences[PreferencesKeys.RECIPES] = Json.encodeToString(recipes)
        }
    }
} 