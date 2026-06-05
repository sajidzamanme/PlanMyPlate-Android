package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.domain.model.GroceryListItem
import com.teamconfused.planmyplate.ui.viewmodels.GroceryViewModel

import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceriesScreen(onNavigateToInventory: () -> Unit) {
    val context = LocalContext.current
    
    val viewModel: GroceryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var buyingItem by remember { mutableStateOf<GroceryListItem?>(null) }

    // Initial load on first composition
    LaunchedEffect(Unit) {
        viewModel.fetchGroceryLists()
    }
    
    // Sync refresh state with loading state
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (uiState.purchaseQuantities.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.purchaseSelectedItems { /* Optional toast or navigation */ } }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(R.drawable.shopping_icon), contentDescription = "Purchase")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Inventory (${uiState.purchaseQuantities.size})")
                    }
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchGroceryLists()
            },
            modifier = Modifier
                .padding(padding)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grocery List",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    TextButton(onClick = onNavigateToInventory) {
                        Text("My Inventory")
                    }
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.activeListItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Your grocery list is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                       items(uiState.activeListItems) { item ->
                           val itemId = item.id ?: 0
                           val buyingQty = uiState.purchaseQuantities[itemId]
                           GroceryItemCard(
                               item = item,
                               isChecked = buyingQty != null,
                               buyingQuantity = buyingQty,
                               onClick = { buyingItem = item }
                           )
                       }
                    }
                }
            }
                
            uiState.errorMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    buyingItem?.let { item ->
        BuyQuantityDialog(
            itemName = item.ingredient?.name ?: "Item",
            neededQuantity = item.quantity ?: 0.0,
            initialBuyingQuantity = uiState.purchaseQuantities[item.id ?: 0],
            unit = item.unit,
            onDismiss = { buyingItem = null },
            onConfirm = { buyingQty ->
                viewModel.setPurchaseQuantity(item.id ?: 0, buyingQty)
                buyingItem = null
            }
        )
    }
}

@Composable
fun GroceryItemCard(
    item: GroceryListItem, 
    isChecked: Boolean,
    buyingQuantity: Double?,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked, 
                onCheckedChange = null // Handled by Card click
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.ingredient?.name ?: "Unknown Item", 
                    style = MaterialTheme.typography.bodyLarge
                )
                val unitStr = if (!item.unit.isNullOrBlank()) " ${item.unit}" else ""
                val quantityText = if (isChecked && buyingQuantity != null) {
                    "$buyingQuantity / ${item.quantity ?: 0.0}$unitStr"
                } else {
                    "${item.quantity ?: 0.0}$unitStr"
                }
                Text(
                    text = quantityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BuyQuantityDialog(
    itemName: String,
    neededQuantity: Double,
    initialBuyingQuantity: Double?,
    unit: String?,
    onDismiss: () -> Unit,
    onConfirm: (Double?) -> Unit
) {
    var quantityText by remember { mutableStateOf(initialBuyingQuantity?.toString()?.removeSuffix(".0") ?: neededQuantity.toString().removeSuffix(".0")) }
    val isValid = quantityText.isEmpty() || (quantityText.toDoubleOrNull() != null && (quantityText.toDoubleOrNull() ?: -1.0) >= 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Buy $itemName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Amount needed: $neededQuantity${if (!unit.isNullOrBlank()) " $unit" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity to Buy") },
                    placeholder = { Text("e.g. 1.0, 2.5") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValid
                )
                if (!isValid) {
                    Text(
                        text = "Please enter a valid non-negative number",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = quantityText.toDoubleOrNull()
                    if (value == null || value <= 0.0) {
                        onConfirm(null)
                    } else {
                        onConfirm(value)
                    }
                },
                enabled = isValid
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Row {
                if (initialBuyingQuantity != null) {
                    TextButton(
                        onClick = { onConfirm(null) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear Selection")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
