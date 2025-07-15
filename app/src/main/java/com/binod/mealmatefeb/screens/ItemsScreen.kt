package com.binod.mealmatefeb.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.binod.mealmatefeb.data.Item
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import com.binod.mealmatefeb.viewmodel.MealPlanViewModel
import com.binod.mealmatefeb.viewmodel.RecipeViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt
import com.binod.mealmatefeb.components.SendSmsDialog
import com.binod.mealmatefeb.MainActivity
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Calendar

@Composable
fun AnalyticsCard(
    recipeCount: Int,
    toBuyCount: Int,
    totalPlannedMeals: Int,
    uniqueRecipes: Int,
    onShoppingListClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Kitchen Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnalyticItem(
                    count = recipeCount.toString(),
                    label = "Recipes"
                )
                AnalyticItem(
                    count = toBuyCount.toString(),
                    label = "To Buy Items"
                )
                AnalyticItem(
                    count = totalPlannedMeals.toString(),
                    label = "Planned\nMeals"
                )
                AnalyticItem(
                    count = uniqueRecipes.toString(),
                    label = "Unique\nRecipes"
                )
            }
            
            Button(
                onClick = onShoppingListClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = "Shopping List",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("View Shopping List")
            }
        }
    }
}

@Composable
private fun AnalyticItem(
    count: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MealOfTheDay(
    mealPlanViewModel: MealPlanViewModel,
    modifier: Modifier = Modifier
) {
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
    
    // Use Calendar for consistency with MealPlannerScreen
    val calendar = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.SUNDAY
    }
    val today = LocalDate.now()
    
    // Get week start date using Calendar
    val weekStartDate = calendar.clone() as Calendar
    weekStartDate.set(Calendar.DAY_OF_WEEK, weekStartDate.firstDayOfWeek)
    weekStartDate.set(Calendar.HOUR_OF_DAY, 0)
    weekStartDate.set(Calendar.MINUTE, 0)
    weekStartDate.set(Calendar.SECOND, 0)
    weekStartDate.set(Calendar.MILLISECOND, 0)
    
    // Find the current week's meal plan
    val currentMealPlan = mealPlans.find { it.weekStartDate == weekStartDate.timeInMillis }
    val todayPlan = currentMealPlan?.dailyPlans?.get(calendar.get(Calendar.DAY_OF_WEEK))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "Today's Meals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("MMM d", Locale.getDefault()).format(calendar.time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (todayPlan == null) {
                Text(
                    text = "No meals planned for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Breakfast
                todayPlan.breakfast?.let { recipe ->
                    MealItem(mealType = "Breakfast", recipe = recipe)
                }
                
                // Lunch
                todayPlan.lunch?.let { recipe ->
                    MealItem(mealType = "Lunch", recipe = recipe)
                }
                
                // Dinner
                todayPlan.dinner?.let { recipe ->
                    MealItem(mealType = "Dinner", recipe = recipe)
                }
                
                if (todayPlan.breakfast == null && todayPlan.lunch == null && todayPlan.dinner == null) {
                    Text(
                        text = "No meals planned for today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MealItem(
    mealType: String,
    recipe: Recipe
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recipe image or placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            if (recipe.imageUri != null) {
                AsyncImage(
                    model = recipe.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = mealType,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun QuickActionsCard(
    onAddRecipe: () -> Unit,
    onPlanMeal: () -> Unit,
    onViewRecipes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Filled.Add,
                    label = "Add Recipe",
                    onClick = onAddRecipe
                )
                ActionButton(
                    icon = Icons.Filled.DateRange,
                    label = "Plan Meal",
                    onClick = onPlanMeal
                )
                ActionButton(
                    icon = Icons.Filled.Menu,
                    label = "Recipes",
                    onClick = onViewRecipes
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun RecentRecipesCard(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recipes.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Recent Recipes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                items(
                    items = recipes.take(5),
                    key = { recipe -> recipe.id }
                ) { recipe ->
                    RecipePreviewCard(recipe = recipe, onClick = onRecipeClick)
                }
            }
        }
    }
}

@Composable
private fun RecipePreviewCard(
    recipe: Recipe,
    onClick: (Recipe) -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(180.dp)
            .clickable { onClick(recipe) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Recipe Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                if (recipe.imageUri != null) {
                    AsyncImage(
                        model = recipe.imageUri,
                        contentDescription = "Recipe Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder when no image is available
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Recipe info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.preparationTime} mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MealStatsCard(
    totalPlannedMeals: Int,
    uniqueRecipes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Meal Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalPlannedMeals.toString(),
                    label = "Planned\nMeals"
                )
                StatItem(
                    value = uniqueRecipes.toString(),
                    label = "Unique\nRecipes"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TipsCard(modifier: Modifier = Modifier) {
    var currentTipIndex by remember { mutableStateOf(0) }
    val tips = listOf(
        "Plan your meals for the week to save time and money",
        "Cook in bulk and freeze portions for busy days",
        "Keep your pantry organized and well-stocked",
        "Try one new recipe each week to expand your menu"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "Tip of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        currentTipIndex = (currentTipIndex + 1) % tips.size
                    }
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Next Tip"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tips[currentTipIndex],
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ItemsScreen(
    itemViewModel: ItemViewModel,
    recipeViewModel: RecipeViewModel,
    mealPlanViewModel: MealPlanViewModel,
    onShoppingListClick: () -> Unit,
    onAddRecipeClick: () -> Unit,
    onPlanMealClick: () -> Unit,
    onViewRecipesClick: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by itemViewModel.items.collectAsState()
    val recipes by recipeViewModel.recipes.collectAsState()
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()

    // Calculate counts for analytics
    val toBuyCount = items.count { !it.isBought }
    val recipeCount = recipes.size
    
    // Calculate meal stats
    val totalPlannedMeals = mealPlans.sumOf { weekPlan ->
        weekPlan.dailyPlans.values.sumOf { dayPlan ->
            listOfNotNull(dayPlan.breakfast, dayPlan.lunch, dayPlan.dinner).size
        }
    }
    val uniqueRecipes = mealPlans.flatMap { weekPlan ->
        weekPlan.dailyPlans.values.flatMap { dayPlan ->
            listOfNotNull(dayPlan.breakfast, dayPlan.lunch, dayPlan.dinner)
        }
    }.distinct().size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AnalyticsCard(
                recipeCount = recipeCount,
                toBuyCount = toBuyCount,
                totalPlannedMeals = totalPlannedMeals,
                uniqueRecipes = uniqueRecipes,
                onShoppingListClick = onShoppingListClick
            )
        }

        item {
            QuickActionsCard(
                onAddRecipe = onAddRecipeClick,
                onPlanMeal = onPlanMealClick,
                onViewRecipes = onViewRecipesClick
            )
        }
        
        item {
            MealOfTheDay(
                mealPlanViewModel = mealPlanViewModel
            )
        }
        
        item {
            RecentRecipesCard(
                recipes = recipes.takeLast(5).reversed(),
                onRecipeClick = onRecipeClick
            )
        }
        
        item {
            TipsCard()
        }
    }
}

@Composable
fun SwipeableItemCard(
    item: Item,
    onEdit: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onToggleBought: (Item) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = 150f
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box {
        // Left side - Green background with edit icon
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.3f)
                .matchParentSize()
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Right side - Red background with delete icon
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.3f)
                .matchParentSize()
                .background(Color(0xFFE53935)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.padding(end = 16.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX >= swipeThreshold -> onEdit(item)
                                offsetX <= -swipeThreshold -> showDeleteDialog = true
                            }
                            offsetX = 0f
                        }
                    ) { _, dragAmount ->
                        val newOffset = offsetX + dragAmount
                        offsetX = newOffset.coerceIn(-swipeThreshold, swipeThreshold)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isBought,
                        onCheckedChange = { onToggleBought(item) }
                    )
                    Text(
                        text = "${item.name} (${item.quantity})",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                }
                if (item.isBought) {
                    Text(
                        text = "Purchased",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(item)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddEditItemDialog(
    item: Item?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Item" else "Edit Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantityInt = quantity.toIntOrNull() ?: 0
                    if (name.isNotBlank() && quantityInt > 0) {
                        onSave(name, quantityInt)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 