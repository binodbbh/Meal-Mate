package com.binod.mealmatefeb.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.data.Recipe
import com.binod.mealmatefeb.data.Ingredient
import com.binod.mealmatefeb.data.IngredientCategory
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDialog(
    recipe: Recipe?,
    onDismiss: () -> Unit,
    onSave: (name: String, ingredients: List<Ingredient>, instructions: List<String>, prepTime: Int, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(recipe?.name ?: "") }
    var prepTime by remember { mutableStateOf(recipe?.preparationTime?.toString() ?: "") }
    var ingredients by remember { mutableStateOf(recipe?.ingredients ?: emptyList()) }
    var instructions by remember { mutableStateOf(recipe?.instructions ?: emptyList()) }
    var imageUri by remember { mutableStateOf(recipe?.imageUri) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }

    // For new ingredient input
    var newIngredientName by remember { mutableStateOf("") }
    var newIngredientQuantity by remember { mutableStateOf("") }
    var newIngredientUnit by remember { mutableStateOf("") }
    var newIngredientCategory by remember { mutableStateOf(IngredientCategory.OTHER) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    
    // For new instruction input
    var newInstruction by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (recipe == null) "Add New Recipe" else "Edit Recipe") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(500.dp)
            ) {
                item {
                    if (showError) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Image Upload Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Recipe Image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            TextButton(
                                onClick = { imageUri = null },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Remove Image")
                            }
                        }
                        
                        Button(
                            onClick = { showImagePicker = true },
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(if (imageUri == null) "Add Image" else "Change Image")
                        }
                    }

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

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoryDropdown,
                        onExpandedChange = { expandedCategoryDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = newIngredientCategory.displayName(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoryDropdown,
                            onDismissRequest = { expandedCategoryDropdown = false }
                        ) {
                            IngredientCategory.values().forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.displayName()) },
                                    onClick = {
                                        newIngredientCategory = category
                                        expandedCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Ingredient inputs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newIngredientName,
                            onValueChange = { newIngredientName = it },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newIngredientQuantity,
                            onValueChange = { newIngredientQuantity = it },
                            label = { Text("Qty") },
                            modifier = Modifier.width(80.dp)
                        )
                        OutlinedTextField(
                            value = newIngredientUnit,
                            onValueChange = { newIngredientUnit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.width(80.dp)
                        )
                    }

                    // Add ingredient button
                    Button(
                        onClick = {
                            if (newIngredientName.isNotBlank() && newIngredientQuantity.isNotBlank()) {
                                ingredients = ingredients + Ingredient(
                                    name = newIngredientName,
                                    quantity = newIngredientQuantity.toDoubleOrNull() ?: 0.0,
                                    unit = newIngredientUnit,
                                    category = newIngredientCategory
                                )
                                newIngredientName = ""
                                newIngredientQuantity = ""
                                newIngredientUnit = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Add Ingredient")
                    }

                    // Display ingredients grouped by category
                    ingredients.groupBy { it.category }.forEach { (groupCategory, groupIngredients) ->
                        Text(
                            text = groupCategory.displayName(),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                        groupIngredients.forEach { ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${ingredient.quantity} ${ingredient.unit} ${ingredient.name}",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    ingredients = ingredients.filter { it != ingredient }
                                }) {
                                    Icon(Icons.Default.Delete, "Remove Ingredient")
                                }
                            }
                        }
                    }

                    // Instructions section
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = newInstruction,
                        onValueChange = { newInstruction = it },
                        label = { Text("Add Instruction") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (newInstruction.isNotBlank()) {
                                instructions = instructions + newInstruction
                                newInstruction = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Add Instruction")
                    }

                    // Display instructions
                    instructions.forEachIndexed { index, instruction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}. $instruction",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                instructions = instructions.filterIndexed { i, _ -> i != index }
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
                            onSave(name, ingredients, instructions, prepTime.toInt(), imageUri)
                            onDismiss()
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

    if (showImagePicker) {
        ImagePickerDialog(
            onImageSelected = { uri ->
                imageUri = uri
                showImagePicker = false
            },
            onDismiss = { showImagePicker = false }
        )
    }
}

@Composable
fun ImagePickerDialog(
    onImageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            // Copy the image to app's private storage
            val copiedUri = copyImageToPrivateStorage(context, it)
            onImageSelected(copiedUri.toString())
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Image Source") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Choose from Gallery")
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

private fun copyImageToPrivateStorage(context: Context, uri: Uri): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
    val fileName = "recipe_image_${System.currentTimeMillis()}.jpg"
    val outputFile = File(context.filesDir.absolutePath, fileName)
    
    inputStream?.use { input ->
        outputFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    
    return Uri.fromFile(outputFile)
} 