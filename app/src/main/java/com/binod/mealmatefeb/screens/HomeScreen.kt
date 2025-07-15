package com.binod.mealmatefeb.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import kotlin.math.sqrt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.binod.mealmatefeb.data.ItemRepository
import com.binod.mealmatefeb.data.RecipeRepository
import com.binod.mealmatefeb.data.UserRepository
import com.binod.mealmatefeb.viewmodel.AuthViewModel
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import com.binod.mealmatefeb.viewmodel.RecipeViewModel
import com.binod.mealmatefeb.screens.MealPlannerScreen
import com.binod.mealmatefeb.data.MealPlanRepository
import com.binod.mealmatefeb.viewmodel.MealPlanViewModel

@Composable
private fun AppTopBar(title: String) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun HomeScreen(
    onScreenChange: (String) -> Unit,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) }
    
    val itemRepository = remember { ItemRepository(context, userRepository) }
    val recipeRepository = remember { RecipeRepository(context, userRepository) }
    val mealPlanRepository = remember { MealPlanRepository(context, userRepository) }
    
    val itemViewModel = remember { ItemViewModel(itemRepository) }
    val recipeViewModel = remember { RecipeViewModel(recipeRepository) }
    val mealPlanViewModel = remember { MealPlanViewModel(mealPlanRepository) }
    
    var selectedTab by remember { mutableStateOf(0) }
    var showShoppingList by remember { mutableStateOf(false) }

    // Shake detection setup
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastUpdate = 0L
        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        val shakeThreshold = 800

        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - lastUpdate) > 100) {
                    val diffTime = currentTime - lastUpdate
                    lastUpdate = currentTime

                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val speed = sqrt(
                        (x - lastX) * (x - lastX) +
                        (y - lastY) * (y - lastY) +
                        (z - lastZ) * (z - lastZ)
                    ) / diffTime * 10000

                    if (speed > shakeThreshold) {
                        selectedTab = 1  // Switch to Recipes screen
                        Toast.makeText(context, "You are successfully redirected to Recipes", Toast.LENGTH_SHORT).show()
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    if (showShoppingList) {
        ShoppingListScreen(
            itemViewModel = itemViewModel,
            onBackClick = { showShoppingList = false }
        )
    } else {
        val currentScreenTitle = when (selectedTab) {
            0 -> "Home"
            1 -> "Recipes"
            2 -> "Meal Planner"
            3 -> "Profile"
            else -> ""
        }
        
        Scaffold(
            topBar = { AppTopBar(title = currentScreenTitle) },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Recipes") },
                        label = { Text("Recipes") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Meal Planner") },
                        label = { Text("Meal Planner") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> ItemsScreen(
                        itemViewModel = itemViewModel,
                        recipeViewModel = recipeViewModel,
                        mealPlanViewModel = mealPlanViewModel,
                        onShoppingListClick = { showShoppingList = true },
                        onAddRecipeClick = { selectedTab = 1 },
                        onPlanMealClick = { selectedTab = 2 },
                        onViewRecipesClick = { selectedTab = 1 },
                        onRecipeClick = { recipe -> 
                            selectedTab = 1
                        }
                    )
                    1 -> RecipesScreen(
                        viewModel = recipeViewModel,
                        itemViewModel = itemViewModel
                    )
                    2 -> MealPlannerScreen(
                        viewModel = recipeViewModel,
                        mealPlanViewModel = mealPlanViewModel,
                        itemViewModel = itemViewModel,
                        onNavigateToShoppingList = { showShoppingList = true }
                    )
                    3 -> ProfileScreen(
                        authViewModel = authViewModel,
                        onLogoutSuccess = { onScreenChange("login") }
                    )
                }
            }
        }
    }
} 