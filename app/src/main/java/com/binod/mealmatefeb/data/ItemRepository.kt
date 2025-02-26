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

class ItemRepository(private val context: Context) {
    private object PreferencesKeys {
        val ITEMS = stringPreferencesKey("items")
    }

    val items: Flow<List<Item>> = context.itemDataStore.data.map { preferences ->
        val itemsString = preferences[PreferencesKeys.ITEMS] ?: "[]"
        try {
            Json.decodeFromString<List<Item>>(itemsString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveItems(items: List<Item>) {
        context.itemDataStore.edit { preferences ->
            preferences[PreferencesKeys.ITEMS] = Json.encodeToString(items)
        }
    }
} 