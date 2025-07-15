package com.binod.mealmatefeb.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.data.*
import com.binod.mealmatefeb.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MealPlannerScreen(
    viewModel: RecipeViewModel,
    mealPlanViewModel: MealPlanViewModel,
    itemViewModel: ItemViewModel,
    modifier: Modifier = Modifier,
    onNavigateToShoppingList: () -> Unit
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
    var showConfirmDialog by remember { mutableStateOf(false) }

    val weekStartDate = getWeekStartDate(selectedDay)
    val currentWeekPlan = mealPlans.find { it.weekStartDate == weekStartDate }
    
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val listState = rememberLazyListState()
    val currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

    LaunchedEffect(Unit) {
        listState.scrollToItem(currentDayIndex)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Week indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Week of ${dateFormat.format(Date(weekStartDate))}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Days of week
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getDaysOfWeek()) { day ->
                val dayNumber = getDayNumber(day)
                val dayCalendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDay.timeInMillis
                    set(Calendar.DAY_OF_WEEK, dayNumber)
                }
                val isToday = dayNumber == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                DayTab(
                    day = day,
                    date = dateFormat.format(dayCalendar.time),
                    isSelected = selectedDay.get(Calendar.DAY_OF_WEEK) == dayNumber,
                    isToday = isToday,
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

        // Selected day's meal plan
        val selectedDayPlan = currentWeekPlan?.dailyPlans?.get(selectedDay.get(Calendar.DAY_OF_WEEK))
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Add Week's Meals to Shopping List")
            }
            
            OutlinedButton(
                onClick = onNavigateToShoppingList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Shopping List")
            }
        }
    }

    // Dialogs
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
                        onNavigateToShoppingList()
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (mealType) {
                MealType.BREAKFAST -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                MealType.LUNCH -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                MealType.DINNER -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    TextButton(onClick = onAddClick) {
                        Text(if (recipe == null) "Add Meal" else "Change")
                    }
                    if (recipe != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove Recipe",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            if (recipe != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Preparation time: ${recipe.preparationTime} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No meal planned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DayTab(
    day: String,
    date: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isToday) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.substring(0, 3),
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
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
    val weekStart = calendar.clone() as Calendar
    weekStart.set(Calendar.DAY_OF_WEEK, weekStart.firstDayOfWeek)
    weekStart.set(Calendar.HOUR_OF_DAY, 0)
    weekStart.set(Calendar.MINUTE, 0)
    weekStart.set(Calendar.SECOND, 0)
    weekStart.set(Calendar.MILLISECOND, 0)
    return weekStart.timeInMillis
} 