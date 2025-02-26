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

class MealPlanRepository(private val context: Context) {
    private object PreferencesKeys {
        val MEAL_PLANS = stringPreferencesKey("meal_plans")
    }

    val mealPlans: Flow<List<MealPlan>> = context.mealPlanDataStore.data.map { preferences ->
        val plansString = preferences[PreferencesKeys.MEAL_PLANS] ?: "[]"
        try {
            Json.decodeFromString<List<MealPlan>>(plansString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveMealPlans(plans: List<MealPlan>) {
        context.mealPlanDataStore.edit { preferences ->
            preferences[PreferencesKeys.MEAL_PLANS] = Json.encodeToString(plans)
        }
    }
} 