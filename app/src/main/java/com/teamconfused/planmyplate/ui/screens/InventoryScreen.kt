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

import com.teamconfused.planmyplate.domain.model.InventoryItem
import com.teamconfused.planmyplate.ui.viewmodels.InventoryViewModel

import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    
    val viewModel: InventoryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Initial load on first composition
    LaunchedEffect(Unit) {
        viewModel.fetchInventory()
    }
    
    // Sync refresh state with loading state
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchInventory()
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = androidx.compose.ui.res.painterResource(com.teamconfused.planmyplate.R.drawable.arrow_back_icon), contentDescription = "Back")
                    }
                    Text(
                        text = "My Inventory",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.items.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Inventory is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(uiState.items) { item ->
                            InventoryItemCard(
                                item = item,
                                onIncrease = { viewModel.updateItemQuantity(item, 1) },
                                onDecrease = { viewModel.updateItemQuantity(item, -1) }
                            )
                        }
                    }
                }
                 uiState.errorMessage?.let { msg ->
                    Text(msg, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.ingredient?.name ?: "Item #${item.ingredient?.ingId ?: "Unknown"}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Expires: ${item.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecrease,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    if ((item.quantity ?: 0.0) <= 1) {
                         Icon(painter = painterResource(com.teamconfused.planmyplate.R.drawable.remove_icon), contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                    } else {
                         // Use standard Remove icon
                         Icon(painter = painterResource(com.teamconfused.planmyplate.R.drawable.remove_icon), contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                
               Text(
                    text = "${item.quantity}${if (!item.unit.isNullOrBlank()) " ${item.unit}" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                IconButton(
                    onClick = onIncrease,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                     Icon(painter = painterResource(com.teamconfused.planmyplate.R.drawable.add_icon), contentDescription = "Increase", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}
