package com.binod.mealmatefeb.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.binod.mealmatefeb.data.Item
import com.binod.mealmatefeb.data.IngredientCategory
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import com.binod.mealmatefeb.components.SendSmsDialog

@Composable
fun ShoppingListScreen(
    itemViewModel: ItemViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by itemViewModel.items.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showSmsDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter items based on search query
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Group filtered items by category
    val groupedItems = filteredItems.groupBy { it.category }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Shopping List") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSmsDialog = true }) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send List via SMS"
                            )
                        }
                    }
                )
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search items...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Item")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Display filtered and grouped items
            IngredientCategory.values().forEach { category ->
                val categoryItems = groupedItems[category]
                if (!categoryItems.isNullOrEmpty()) {
                    item {
                        Text(
                            text = category.displayName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    items(categoryItems) { item ->
                        SwipeableItemCard(
                            item = item,
                            onEdit = { itemToEdit = it },
                            onDelete = { itemViewModel.deleteItem(it) },
                            onToggleBought = { itemViewModel.toggleItemBought(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEditItemDialog(
                item = null,
                onDismiss = { showAddDialog = false },
                onSave = { name, quantity, category ->
                    itemViewModel.addItem(name, quantity, category)
                    showAddDialog = false
                }
            )
        }

        if (itemToEdit != null) {
            AddEditItemDialog(
                item = itemToEdit,
                onDismiss = { itemToEdit = null },
                onSave = { name, quantity, category ->
                    itemViewModel.updateItem(itemToEdit!!.copy(
                        name = name,
                        quantity = quantity,
                        category = category
                    ))
                    itemToEdit = null
                }
            )
        }

        if (showSmsDialog) {
            SendSmsDialog(
                items = items.filter { !it.isBought },
                onDismiss = { showSmsDialog = false },
                onPermissionNeeded = {
                    Toast.makeText(context, "SMS permission needed", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

private fun buildShareText(items: List<Item>): String {
    return buildString {
        append("Shopping List:\n\n")
        val groupedItems = items.groupBy { it.category }
        IngredientCategory.values().forEach { category ->
            val categoryItems = groupedItems[category]
            if (!categoryItems.isNullOrEmpty()) {
                append("${category.displayName()}:\n")
                categoryItems.forEach { item ->
                    append("• ${item.name} (${item.quantity})\n")
                }
                append("\n")
            }
        }
    }
}

@Composable
fun AddEditItemDialog(
    item: Item?,
    onDismiss: () -> Unit,
    onSave: (String, Int, IngredientCategory) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(item?.category ?: IngredientCategory.OTHER) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

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
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        IngredientCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName()) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val quantityInt = quantity.toIntOrNull() ?: 0
                    if (name.isNotBlank() && quantityInt > 0) {
                        onSave(name, quantityInt, selectedCategory)
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