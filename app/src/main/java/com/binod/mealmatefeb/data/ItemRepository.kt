package com.binod.mealmatefeb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.itemDataStore: DataStore<Preferences> by preferencesDataStore(name = "items")

class ItemRepository(
    private val context: Context,
    private val userRepository: UserRepository
) {
    private object PreferencesKeys {
        val ITEMS_PREFIX = stringPreferencesKey("items_") // Base key for user-specific items
    }

    private suspend fun getCurrentUserItemsKey(): Preferences.Key<String> {
        val currentUser = userRepository.getCurrentUser() ?: return PreferencesKeys.ITEMS_PREFIX
        return stringPreferencesKey("items_${currentUser.email}")
    }

    val items: Flow<List<Item>> = context.itemDataStore.data.map { preferences ->
        val key = getCurrentUserItemsKey()
        val itemsString = preferences[key] ?: "[]"
        try {
            Json.decodeFromString<List<Item>>(itemsString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveItems(items: List<Item>) {
        val key = getCurrentUserItemsKey()
        context.itemDataStore.edit { preferences ->
            preferences[key] = Json.encodeToString(items)
        }
    }

    suspend fun clearItems() {
        val key = getCurrentUserItemsKey()
        context.itemDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
} 