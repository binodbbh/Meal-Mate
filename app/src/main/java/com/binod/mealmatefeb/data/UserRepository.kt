package com.binod.mealmatefeb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "users")

class UserRepository(private val context: Context) {
    private object PreferencesKeys {
        val USERS = stringPreferencesKey("users")
        val LOGGED_IN_USER = stringPreferencesKey("logged_in_user")
    }

    suspend fun registerUser(user: User): Boolean {
        val users = getUsers().toMutableList()
        if (users.any { it.email == user.email }) {
            return false
        }
        users.add(user)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERS] = users.joinToString(";") { "${it.email}|${it.password}|${it.name}" }
        }
        return true
    }

    suspend fun getCurrentUser(): User? {
        return context.dataStore.data.map { preferences ->
            val loggedInUserEmail = preferences[PreferencesKeys.LOGGED_IN_USER] ?: return@map null
            getUsers().find { it.email == loggedInUserEmail }
        }.first()
    }

    suspend fun loginUser(email: String, password: String): User? {
        val users = getUsers()
        val user = users.find { it.email == email && it.password == password }
        if (user != null) {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.LOGGED_IN_USER] = user.email
            }
        }
        return user
    }

    suspend fun getUsers(): List<User> {
        return context.dataStore.data.map { preferences ->
            val usersString = preferences[PreferencesKeys.USERS] ?: ""
            if (usersString.isEmpty()) emptyList()
            else usersString.split(";").map { userStr ->
                val (email, password, name) = userStr.split("|")
                User(email, password, name)
            }
        }.first()
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LOGGED_IN_USER)
        }
        // Clear all user-specific data when logging out
        val itemRepository = ItemRepository(context, this)
        val recipeRepository = RecipeRepository(context, this)
        val mealPlanRepository = MealPlanRepository(context, this)
        
        itemRepository.clearItems()
        recipeRepository.clearRecipes()
        mealPlanRepository.clearMealPlans()
    }
} 