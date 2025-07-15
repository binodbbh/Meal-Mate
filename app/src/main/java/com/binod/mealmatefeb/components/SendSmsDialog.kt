package com.binod.mealmatefeb.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.binod.mealmatefeb.data.Item
import android.content.Intent
import android.net.Uri

@Composable
fun SendSmsDialog(
    items: List<Item>,
    onDismiss: () -> Unit,
    onPermissionNeeded: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Shopping List") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text("Shop Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = totalPrice,
                    onValueChange = { totalPrice = it },
                    label = { Text("Total Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Additional Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Text("Items to share:", style = MaterialTheme.typography.bodyLarge)
                items.forEach { item ->
                    Text("• ${item.name} (${item.quantity})")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalMessage = buildFinalMessage(
                        items,
                        message,
                        shopName,
                        shopAddress,
                        totalPrice
                    )
                    shareShoppingList(context, finalMessage)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share List")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun shareShoppingList(context: Context, message: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share shopping list via"))
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error sharing shopping list: ${e.localizedMessage ?: "Unknown error"}",
            Toast.LENGTH_LONG
        ).show()
        e.printStackTrace()
    }
}

private fun buildFinalMessage(
    items: List<Item>,
    additionalMessage: String,
    shopName: String,
    shopAddress: String,
    totalPrice: String
): String {
    val itemsList = items.joinToString("\n") { "• ${it.name} (${it.quantity})" }
    val shopDetails = buildString {
        if (shopName.isNotEmpty()) append("\nShop: $shopName")
        if (shopAddress.isNotEmpty()) append("\nAddress: $shopAddress")
        if (totalPrice.isNotEmpty()) append("\nTotal Price: $$totalPrice")
    }
    
    return buildString {
        if (additionalMessage.isNotBlank()) {
            append(additionalMessage)
            append("\n\n")
        }
        append("Shopping List:")
        append("\n")
        append(itemsList)
        if (shopDetails.isNotEmpty()) {
            append("\n")
            append(shopDetails)
        }
    }
} 