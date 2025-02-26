package com.binod.mealmatefeb.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.data.Ingredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDialog(
    recipe: Recipe?,
    onDismiss: () -> Unit,
    onSave: (name: String, ingredients: List<Ingredient>, instructions: List<String>, prepTime: Int) -> Unit
) {
    var name by remember { mutableStateOf(recipe?.name ?: "") }
    var prepTime by remember { mutableStateOf(recipe?.preparationTime?.toString() ?: "") }
    var ingredients by remember { mutableStateOf(recipe?.ingredients ?: emptyList()) }
    var instructions by remember { mutableStateOf(recipe?.instructions ?: emptyList()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // For new ingredient input
    var newIngredientName by remember { mutableStateOf("") }
    var newIngredientQuantity by remember { mutableStateOf("") }
    var newIngredientUnit by remember { mutableStateOf("") }
    
    // For new instruction input
    var newInstruction by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (recipe == null) "Add New Recipe" else "Edit Recipe") },
        text = {
            Column {
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Recipe Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = prepTime,
                            onValueChange = { prepTime = it },
                            label = { Text("Preparation Time (minutes)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        // Ingredients Section
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        // Add new ingredient inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newIngredientName,
                                onValueChange = { newIngredientName = it },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = newIngredientQuantity,
                                onValueChange = { newIngredientQuantity = it },
                                label = { Text("Qty") },
                                modifier = Modifier.width(80.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = newIngredientUnit,
                                onValueChange = { newIngredientUnit = it },
                                label = { Text("Unit") },
                                modifier = Modifier.width(80.dp)
                            )
                            IconButton(onClick = {
                                if (newIngredientName.isNotBlank() && newIngredientQuantity.isNotBlank()) {
                                    ingredients = ingredients + Ingredient(
                                        name = newIngredientName,
                                        quantity = newIngredientQuantity.toDoubleOrNull() ?: 0.0,
                                        unit = newIngredientUnit
                                    )
                                    newIngredientName = ""
                                    newIngredientQuantity = ""
                                    newIngredientUnit = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, "Add Ingredient")
                            }
                        }
                    }

                    // List of added ingredients
                    items(ingredients) { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${ingredient.quantity} ${ingredient.unit} ${ingredient.name}")
                            IconButton(onClick = {
                                ingredients = ingredients.filter { it != ingredient }
                            }) {
                                Icon(Icons.Default.Delete, "Remove Ingredient")
                            }
                        }
                    }

                    item {
                        // Instructions Section
                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        // Add new instruction input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newInstruction,
                                onValueChange = { newInstruction = it },
                                label = { Text("Instruction Step") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                if (newInstruction.isNotBlank()) {
                                    instructions = instructions + newInstruction
                                    newInstruction = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, "Add Instruction")
                            }
                        }
                    }

                    // List of added instructions
                    itemsIndexed(instructions) { index, instruction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}. $instruction")
                            IconButton(onClick = {
                                instructions = instructions.filter { it != instruction }
                            }) {
                                Icon(Icons.Default.Delete, "Remove Instruction")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        name.isBlank() -> {
                            showError = true
                            errorMessage = "Please enter a recipe name"
                        }
                        ingredients.isEmpty() -> {
                            showError = true
                            errorMessage = "Please add at least one ingredient"
                        }
                        instructions.isEmpty() -> {
                            showError = true
                            errorMessage = "Please add at least one instruction"
                        }
                        prepTime.toIntOrNull() == null -> {
                            showError = true
                            errorMessage = "Please enter a valid preparation time"
                        }
                        else -> {
                            showError = false
                            onSave(name, ingredients, instructions, prepTime.toIntOrNull() ?: 0)
                        }
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