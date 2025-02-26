package com.binod.mealmatefeb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.components.RecipeCard
import com.binod.mealmatefeb.components.RecipeDialog
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.data.Ingredient
import com.binod.mealmatefeb.viewmodel.RecipeViewModel
import com.binod.mealmatefeb.viewmodel.ItemViewModel

@Composable
fun RecipesScreen(
    viewModel: RecipeViewModel,
    itemViewModel: ItemViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    val recipes by viewModel.recipes.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Recipe")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onEditClick = { selectedRecipe = recipe },
                    onDeleteClick = { viewModel.deleteRecipe(recipe) },
                    onGenerateListClick = {
                        viewModel.generateShoppingList(recipe, itemViewModel)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showAddDialog) {
            RecipeDialog(
                recipe = null,
                onDismiss = { showAddDialog = false },
                onSave = { name, ingredients, instructions, prepTime ->
                    viewModel.addRecipe(name, ingredients, instructions, prepTime)
                    showAddDialog = false
                }
            )
        }

        selectedRecipe?.let { recipe ->
            RecipeDialog(
                recipe = recipe,
                onDismiss = { selectedRecipe = null },
                onSave = { name, ingredients, instructions, prepTime ->
                    viewModel.updateRecipe(recipe.copy(
                        name = name,
                        ingredients = ingredients,
                        instructions = instructions,
                        preparationTime = prepTime
                    ))
                    selectedRecipe = null
                }
            )
        }
    }
} 