package com.binod.mealmatefeb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.data.RecipeRepository
import com.binod.mealmatefeb.data.Ingredient
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    val recipes: StateFlow<List<Recipe>> = repository.recipes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addRecipe(name: String, ingredients: List<Ingredient>, instructions: List<String>, prepTime: Int) {
        viewModelScope.launch {
            val newRecipe = Recipe(
                name = name,
                ingredients = ingredients,
                instructions = instructions,
                preparationTime = prepTime
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

    fun generateShoppingList(recipe: Recipe, itemViewModel: ItemViewModel) {
        viewModelScope.launch {
            recipe.ingredients.forEach { ingredient ->
                // Convert ingredient quantity to Int (rounding up)
                val quantity = kotlin.math.ceil(ingredient.quantity).toInt()
                val name = "${ingredient.name} (${ingredient.unit})"
                itemViewModel.addItem(name, quantity)
            }
        }
    }
} 