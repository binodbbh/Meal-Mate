package com.binod.mealmatefeb.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.data.RecipeRepository
import com.binod.mealmatefeb.data.Ingredient
import com.binod.mealmatefeb.data.Item
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    val recipes: StateFlow<List<Recipe>> = repository.recipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Ensure recipes are loaded when ViewModel is created
        viewModelScope.launch {
            if (recipes.value.isEmpty()) {
                repository.recipes.collect { }  // This will trigger the flow and load recipes
            }
        }
    }

    fun addRecipe(name: String, ingredients: List<Ingredient>, instructions: List<String>, prepTime: Int, imageUri: String?) {
        viewModelScope.launch {
            val newRecipe = Recipe(
                name = name,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = prepTime,
                imageUri = imageUri
            )
            repository.saveRecipes(recipes.value + newRecipe)
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.saveRecipes(recipes.value.map { 
                if (it.id == recipe.id) recipe else it 
            })
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.saveRecipes(recipes.value.filter { it.id != recipe.id })
        }
    }

    fun generateShoppingList(recipe: Recipe, itemViewModel: ItemViewModel, context: Context) {
        viewModelScope.launch {
            // Get current items
            val currentItems = itemViewModel.items.value
            
            // Convert recipe ingredients to shopping list items
            val newItems = recipe.ingredients.map { ingredient ->
                val quantity = kotlin.math.ceil(ingredient.quantity).toInt()
                Item(
                    name = "${ingredient.name} (${ingredient.unit})",
                    quantity = quantity,
                    category = ingredient.category
                )
            }
            
            // Combine with existing items, summing quantities for items with the same name and category
            val combinedItems = (currentItems + newItems)
                .groupBy { it.name to it.category }
                .map { (key, items) ->
                    val totalQuantity = items.sumOf { it.quantity }
                    items.first().copy(quantity = totalQuantity)
                }
            
            // Save all items at once
            itemViewModel.saveAllItems(combinedItems)
            
            // Show success message
            Toast.makeText(
                context,
                "Added ${recipe.name} ingredients to shopping list",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
} 