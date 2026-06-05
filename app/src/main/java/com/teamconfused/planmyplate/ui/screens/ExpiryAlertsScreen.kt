package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teamconfused.planmyplate.domain.model.ExpiryItem
import com.teamconfused.planmyplate.ui.viewmodels.ExpiryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryAlertsScreen(
    viewModel: ExpiryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchSoonToExpireItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Expiry Alerts", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.teamconfused.planmyplate.R.drawable.arrow_back_icon),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val soonToExpireResult = uiState.soonToExpire
                val items = soonToExpireResult?.items ?: emptyList()

                if (items.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.teamconfused.planmyplate.R.drawable.check_circle_icon),
                            contentDescription = "All Good",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Expiry Warnings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All items in your pantry are fresh and safe.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Alert Summary",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Threshold: Within ${uiState.warningDays} days",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Expired Items: ${soonToExpireResult?.expiredCount ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        items(items) { item ->
                            ExpiryWarningCard(item = item, onDelete = {
                                item.itemId?.let { viewModel.deleteExpiryItem(it) }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiryWarningCard(
    item: ExpiryItem,
    onDelete: () -> Unit
) {
    val isExpired = item.isExpired ?: false
    val cardColor = if (isExpired) {
        Color(0xFFFFEBEE) // light red
    } else {
        Color(0xFFFFF3E0) // light orange/yellow
    }
    
    val badgeColor = if (isExpired) Color(0xFFC62828) else Color(0xFFEF6C00)
    val badgeText = if (isExpired) "Expired" else "Expiring Soon"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.productName ?: "Unknown Product",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Expiry Date: ${item.expiryDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                if (item.quantity != null) {
                    Text(
                        text = "Quantity: ${item.quantity} ${item.unit ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
                item.daysUntilExpiry?.let { days ->
                    Text(
                        text = if (days < 0) "Expired ${-days} days ago" else "Expires in $days days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.teamconfused.planmyplate.R.drawable.delete_icon),
                    contentDescription = "Delete",
                    tint = Color.Gray
                )
            }
        }
    }
}
