package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
            if (uiState.checkedItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.purchaseSelectedItems { /* Optional toast or navigation */ } }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(R.drawable.shopping_icon), contentDescription = "Purchase")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Inventory (${uiState.checkedItems.size})")
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
            modifier = Modifier.padding(padding)
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
                           GroceryItemCard(
                               item = item,
                               isChecked = uiState.checkedItems.contains(item.id ?: 0),
                               onToggle = { viewModel.toggleItemCheck(item.id ?: 0) },
                               onIncrease = { viewModel.updateListQuantity(item, 1.0) },
                               onDecrease = { viewModel.updateListQuantity(item, -1.0) }
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
}

@Composable
fun GroceryItemCard(
    item: GroceryListItem, 
    isChecked: Boolean, 
    onToggle: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isChecked, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.ingredient?.name ?: "Unknown Item", 
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!item.unit.isNullOrBlank()) {
                    Text(
                        text = item.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Quantity Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecrease,
                     modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Icon(painter = painterResource(com.teamconfused.planmyplate.R.drawable.remove_icon), contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                
                Text(
                    text = "${item.quantity ?: 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                     Icon(painter = painterResource(com.teamconfused.planmyplate.R.drawable.add_icon), contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
