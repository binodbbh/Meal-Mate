package com.binod.mealmatefeb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binod.mealmatefeb.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MealPlanViewModel(private val repository: MealPlanRepository) : ViewModel() {
    val mealPlans: StateFlow<List<MealPlan>> = repository.mealPlans
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addOrUpdateMealPlan(weekStartDate: Long, dayOfWeek: Int, mealType: MealType, recipe: Recipe?) {
        viewModelScope.launch {
            val currentPlans = mealPlans.value.toMutableList()
            val existingPlan = currentPlans.find { it.weekStartDate == weekStartDate }

            if (existingPlan != null) {
                // Update existing plan
                val updatedDailyPlans = existingPlan.dailyPlans.toMutableMap()
                val currentDayPlan = updatedDailyPlans[dayOfWeek] ?: DayPlan()
                
                val updatedDayPlan = when (mealType) {
                    MealType.BREAKFAST -> currentDayPlan.copy(breakfast = recipe)
                    MealType.LUNCH -> currentDayPlan.copy(lunch = recipe)
                    MealType.DINNER -> currentDayPlan.copy(dinner = recipe)
                }
                
                updatedDailyPlans[dayOfWeek] = updatedDayPlan
                val updatedPlan = existingPlan.copy(dailyPlans = updatedDailyPlans)
                currentPlans[currentPlans.indexOf(existingPlan)] = updatedPlan
            } else {
                // Create new plan
                val newDayPlan = when (mealType) {
                    MealType.BREAKFAST -> DayPlan(breakfast = recipe)
                    MealType.LUNCH -> DayPlan(lunch = recipe)
                    MealType.DINNER -> DayPlan(dinner = recipe)
                }
                
                val newPlan = MealPlan(
                    weekStartDate = weekStartDate,
                    dailyPlans = mapOf(dayOfWeek to newDayPlan)
                )
                currentPlans.add(newPlan)
            }
            
            repository.saveMealPlans(currentPlans)
        }
    }

    fun addIngredientsToShoppingList(currentWeekPlan: MealPlan?, itemViewModel: ItemViewModel) {
        viewModelScope.launch {
            // Get all recipes from the week's meal plan
            val allRecipes = currentWeekPlan?.dailyPlans?.values?.flatMap { dayPlan ->
                listOfNotNull(dayPlan.breakfast, dayPlan.lunch, dayPlan.dinner)
            } ?: return@launch

            // Create a list to hold all ingredients
            val allIngredients = mutableListOf<Ingredient>()

            // Collect all ingredients first
            allRecipes.forEach { recipe ->
                recipe.ingredients.forEach { ingredient ->
                    val quantity = kotlin.math.ceil(ingredient.quantity).toInt()
                    allIngredients.add(ingredient.copy(quantity = quantity.toDouble()))
                }
            }

            // Group ingredients by name and category, summing quantities
            val groupedIngredients = allIngredients.groupBy { it.name }.map { (name, ingredients) ->
                val totalQuantity = ingredients.sumOf { it.quantity }.toInt()
                val firstIngredient = ingredients.first()
                Item(
                    name = name,
                    quantity = totalQuantity,
                    category = firstIngredient.category
                )
            }
            
            // Save all items at once
            itemViewModel.saveAllItems(groupedIngredients)
        }
    }
} 