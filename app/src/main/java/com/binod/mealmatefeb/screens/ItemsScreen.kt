package com.binod.mealmatefeb.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.binod.mealmatefeb.data.Item
import com.binod.mealmatefeb.viewmodel.ItemViewModel
import androidx.compose.material.icons.filled.Share
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

@Composable
fun ItemsScreen(
    itemViewModel: ItemViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    val items by itemViewModel.items.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                actions = {
                    IconButton(onClick = {
                        val unboughtItems = items.filter { !it.isBought }
                        if (unboughtItems.isNotEmpty()) {
                            val shareText = buildString {
                                append("Shopping List:\n\n")
                                unboughtItems.forEach { item ->
                                    append("• ${item.name} (${item.quantity})\n")
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Shopping List", shareText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "List copied to clipboard!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No items to share", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share List")
                    }
                }
            )
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
                .padding(16.dp)
        ) {
            items(items) { item ->
                SwipeableItemCard(
                    item = item,
                    onEdit = { itemToEdit = it },
                    onDelete = { itemViewModel.deleteItem(it) },
                    onToggleBought = { itemViewModel.toggleItemBought(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showAddDialog) {
            AddEditItemDialog(
                item = null,
                onDismiss = { showAddDialog = false },
                onSave = { name, quantity ->
                    itemViewModel.addItem(name, quantity)
                    showAddDialog = false
                }
            )
        }

        if (itemToEdit != null) {
            AddEditItemDialog(
                item = itemToEdit,
                onDismiss = { itemToEdit = null },
                onSave = { name, quantity ->
                    itemViewModel.updateItem(itemToEdit!!.copy(
                        name = name,
                        quantity = quantity
                    ))
                    itemToEdit = null
                }
            )
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
                Icons.Default.Edit,
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
                Icons.Default.Delete,
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