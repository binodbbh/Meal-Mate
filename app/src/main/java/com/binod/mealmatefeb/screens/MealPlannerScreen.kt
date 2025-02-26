package com.binod.mealmatefeb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.data.*
import java.text.SimpleDateFormat
import java.util.*
import com.binod.mealmatefeb.viewmodel.RecipeViewModel
import com.binod.mealmatefeb.components.RecipeCard
import com.binod.mealmatefeb.components.GroceryListDialog
import com.binod.mealmatefeb.viewmodel.MealPlanViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.clickable
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import kotlinx.coroutines.launch

@Composable
fun MealPlannerScreen(
    viewModel: RecipeViewModel,
    mealPlanViewModel: MealPlanViewModel,
    itemViewModel: ItemViewModel,
    modifier: Modifier = Modifier
) {
    val recipes by viewModel.recipes.collectAsState()
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
    var selectedDay by remember { 
        mutableStateOf(Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
        })
    }
    var showRecipeDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }
    var showGroceryList by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val weekStartDate = getWeekStartDate(selectedDay)
    val currentWeekPlan = mealPlans.find { it.weekStartDate == weekStartDate }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Weekly Meal Planner",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Horizontal scrollable days
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getDaysOfWeek()) { day ->
                val dayNumber = getDayNumber(day)
                DayTab(
                    day = day,
                    isSelected = selectedDay.get(Calendar.DAY_OF_WEEK) == dayNumber,
                    onClick = {
                        val newCalendar = Calendar.getInstance().apply {
                            firstDayOfWeek = Calendar.SUNDAY
                            timeInMillis = selectedDay.timeInMillis
                            set(Calendar.DAY_OF_WEEK, dayNumber)
                        }
                        selectedDay = newCalendar
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected day's meal plan
        val selectedDayPlan = currentWeekPlan?.dailyPlans?.get(selectedDay.get(Calendar.DAY_OF_WEEK))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MealType.values().toList()) { mealType ->
                val recipe = when (mealType) {
                    MealType.BREAKFAST -> selectedDayPlan?.breakfast
                    MealType.LUNCH -> selectedDayPlan?.lunch
                    MealType.DINNER -> selectedDayPlan?.dinner
                }
                
                MealSlotCard(
                    mealType = mealType,
                    recipe = recipe,
                    onAddClick = {
                        selectedMealType = mealType
                        showRecipeDialog = true
                    },
                    onDeleteClick = {
                        mealPlanViewModel.addOrUpdateMealPlan(
                            weekStartDate = weekStartDate,
                            dayOfWeek = selectedDay.get(Calendar.DAY_OF_WEEK),
                            mealType = mealType,
                            recipe = null
                        )
                    }
                )
            }
        }

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text("Add to Shopping List")
        }
    }

    if (showRecipeDialog && selectedMealType != null) {
        RecipeSelectionDialog(
            recipes = recipes,
            onDismiss = { showRecipeDialog = false },
            onRecipeSelected = { recipe ->
                mealPlanViewModel.addOrUpdateMealPlan(
                    weekStartDate = weekStartDate,
                    dayOfWeek = selectedDay.get(Calendar.DAY_OF_WEEK),
                    mealType = selectedMealType!!,
                    recipe = recipe
                )
                showRecipeDialog = false
            }
        )
    }

    if (showGroceryList) {
        GroceryListDialog(
            selectedRecipes = currentWeekPlan?.dailyPlans?.values?.flatMap { dayPlan ->
                listOfNotNull(dayPlan.breakfast, dayPlan.lunch, dayPlan.dinner)
            } ?: emptyList(),
            onDismiss = { showGroceryList = false }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Add to Shopping List") },
            text = { Text("Do you want to add all ingredients from this week's meal plan to your shopping list?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mealPlanViewModel.addIngredientsToShoppingList(currentWeekPlan, itemViewModel)
                        showConfirmDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun MealSlotCard(
    mealType: MealType,
    recipe: Recipe?,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = mealType.name.capitalize(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = recipe?.name ?: "No meal planned",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row {
                TextButton(onClick = onAddClick) {
                    Text(if (recipe == null) "Add" else "Change")
                }
                if (recipe != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, "Remove Recipe")
                    }
                }
            }
        }
    }
}

@Composable
fun DayTab(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getDaysOfWeek(): List<String> {
    return listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
}

private fun getDayNumber(dayName: String): Int {
    return when (dayName) {
        "Sunday" -> Calendar.SUNDAY
        "Monday" -> Calendar.MONDAY
        "Tuesday" -> Calendar.TUESDAY
        "Wednesday" -> Calendar.WEDNESDAY
        "Thursday" -> Calendar.THURSDAY
        "Friday" -> Calendar.FRIDAY
        "Saturday" -> Calendar.SATURDAY
        else -> Calendar.SUNDAY
    }
}

private fun getWeekStartDate(calendar: Calendar): Long {
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val diff = Calendar.SUNDAY - dayOfWeek
    val weekStart = calendar.clone() as Calendar
    weekStart.add(Calendar.DAY_OF_WEEK, diff)
    weekStart.set(Calendar.HOUR_OF_DAY, 0)
    weekStart.set(Calendar.MINUTE, 0)
    weekStart.set(Calendar.SECOND, 0)
    weekStart.set(Calendar.MILLISECOND, 0)
    return weekStart.timeInMillis
}

@Composable
fun RecipeSelectionDialog(
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onRecipeSelected: (Recipe) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Recipe") },
        text = {
            LazyColumn {
                items(recipes) { recipe ->
                    TextButton(
                        onClick = { onRecipeSelected(recipe) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(recipe.name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 