package com.binod.mealmatefeb.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.data.Ingredient

@Composable
fun GroceryListDialog(
    selectedRecipes: List<Recipe>,
    onDismiss: () -> Unit
) {
    val aggregatedIngredients = remember(selectedRecipes) {
        selectedRecipes.flatMap { it.ingredients }.groupBy { ingredient ->
            ingredient.name to ingredient.unit
        }.map { (key, ingredients) ->
            val totalQuantity = ingredients.sumOf { it.quantity }
            Ingredient(
                name = key.first,
                quantity = totalQuantity,
                unit = key.second
            )
        }.groupBy { ingredient ->
            when (ingredient.unit.lowercase()) {
                "kg", "g", "oz", "lb" -> "Proteins & Dry Goods"
                "ml", "l", "cup", "tbsp", "tsp" -> "Liquids & Measures"
                "piece", "pieces", "pcs" -> "Count Items"
                else -> "Others"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weekly Shopping List") },
        text = {
            LazyColumn {
                aggregatedIngredients.forEach { (category, ingredients) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(ingredients) { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "• ${ingredient.name}",
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${ingredient.quantity} ${ingredient.unit}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
} 