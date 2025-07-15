package com.binod.mealmatefeb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.mealPlanDataStore: DataStore<Preferences> by preferencesDataStore(name = "meal_plans")

class MealPlanRepository(
    private val context: Context,
    private val userRepository: UserRepository
) {
    private fun getUserMealPlanKey(email: String): Preferences.Key<String> {
        return stringPreferencesKey("meal_plans_$email")
    }

    private suspend fun getCurrentUserMealPlanKey(): Preferences.Key<String> {
        val currentUser = userRepository.getCurrentUser()
        return getUserMealPlanKey(currentUser?.email ?: "default")
    }

    val mealPlans: Flow<List<MealPlan>> = context.mealPlanDataStore.data.map { preferences ->
        val key = getCurrentUserMealPlanKey()
        val plansString = preferences[key] ?: "[]"
        try {
            Json.decodeFromString<List<MealPlan>>(plansString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveMealPlans(plans: List<MealPlan>) {
        val key = getCurrentUserMealPlanKey()
        context.mealPlanDataStore.edit { preferences ->
            preferences[key] = Json.encodeToString(plans)
        }
    }

    suspend fun clearMealPlans() {
        val key = getCurrentUserMealPlanKey()
        context.mealPlanDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
} 