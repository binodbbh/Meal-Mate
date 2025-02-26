package com.binod.mealmatefeb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binod.mealmatefeb.data.Item
import com.binod.mealmatefeb.data.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {
    val items: StateFlow<List<Item>> = repository.items
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addItem(name: String, quantity: Int) {
        viewModelScope.launch {
            val newItem = Item(name = name, quantity = quantity)
            repository.saveItems(items.value + newItem)
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.saveItems(items.value.map { 
                if (it.id == item.id) item else it 
            })
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.saveItems(items.value.filter { it.id != item.id })
        }
    }

    fun toggleItemBought(item: Item) {
        viewModelScope.launch {
            val updatedItem = item.copy(isBought = !item.isBought)
            repository.saveItems(items.value.map { 
                if (it.id == item.id) updatedItem else it 
            })
        }
    }

    fun saveAllItems(newItems: List<Item>) {
        viewModelScope.launch {
            repository.saveItems(newItems)
        }
    }
} 