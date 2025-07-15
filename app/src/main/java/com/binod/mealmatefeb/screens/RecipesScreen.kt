package com.binod.mealmatefeb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.components.RecipeCard
import com.binod.mealmatefeb.components.RecipeDialog
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.data.Ingredient
import com.binod.mealmatefeb.viewmodel.RecipeViewModel
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RecipesScreen(
    viewModel: RecipeViewModel,
    itemViewModel: ItemViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf(SortOption.NAME) }
    val recipes by viewModel.recipes.collectAsState()
    val context = LocalContext.current

    val filteredAndSortedRecipes = remember(recipes, searchQuery, selectedSortOption) {
        recipes.filter { recipe ->
            recipe.name.contains(searchQuery, ignoreCase = true) ||
            recipe.ingredients.any { it.name.contains(searchQuery, ignoreCase = true) }
        }.let { filtered ->
            when (selectedSortOption) {
                SortOption.NAME -> filtered.sortedBy { it.name }
                SortOption.PREP_TIME -> filtered.sortedBy { it.preparationTime }
                SortOption.INGREDIENTS -> filtered.sortedBy { it.ingredients.size }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search recipes...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    singleLine = true
                )
                
                // Sort options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortOption.values().forEach { option ->
                        FilterChip(
                            selected = selectedSortOption == option,
                            onClick = { selectedSortOption = option },
                            label = { Text(option.displayName) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Add Recipe")
            }
        }
    ) { paddingValues ->
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "No recipes",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recipes yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add your first recipe by clicking the + button",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredAndSortedRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onEditClick = { selectedRecipe = recipe },
                        onDeleteClick = { viewModel.deleteRecipe(recipe) },
                        onGenerateListClick = {
                            viewModel.generateShoppingList(recipe, itemViewModel, context)
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            RecipeDialog(
                recipe = null,
                onDismiss = { showAddDialog = false },
                onSave = { name, ingredients, instructions, prepTime, imageUri ->
                    viewModel.addRecipe(name, ingredients, instructions, prepTime, imageUri)
                    showAddDialog = false
                }
            )
        }

        selectedRecipe?.let { recipe ->
            RecipeDialog(
                recipe = recipe,
                onDismiss = { selectedRecipe = null },
                onSave = { name, ingredients, instructions, prepTime, imageUri ->
                    viewModel.updateRecipe(recipe.copy(
                        name = name,
                        ingredients = ingredients,
                        instructions = instructions,
                        preparationTime = prepTime,
                        imageUri = imageUri
                    ))
                    selectedRecipe = null
                }
            )
        }
    }
}

enum class SortOption(val displayName: String) {
    NAME("Name"),
    PREP_TIME("Prep Time"),
    INGREDIENTS("Ingredients")
} 