package com.teamconfused.planmyplate.ui.screens

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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange

import com.teamconfused.planmyplate.domain.model.InventoryItem
import com.teamconfused.planmyplate.ui.viewmodels.ExpiryViewModel
import com.teamconfused.planmyplate.ui.viewmodels.InventoryViewModel

import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    
    val viewModel: InventoryViewModel = koinViewModel()
    val expiryViewModel: ExpiryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<InventoryItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(
                    painter = painterResource(com.teamconfused.planmyplate.R.drawable.add_icon),
                    contentDescription = "Add Item to Pantry",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
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
                    .statusBarsPadding()
                    .navigationBarsPadding()
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
                                onClick = { editingItem = item }
                            )
                        }
                    }
                }
            }
        }
    }

    val searchResults by viewModel.searchResults.collectAsState()

    if (showAddDialog) {
        AddPantryItemDialog(
            searchResults = searchResults,
            onSearchQueryChange = { viewModel.searchIngredients(it) },
            onDismiss = { showAddDialog = false },
            onConfirm = { name, date, qty, unit ->
                val quantityVal = qty.toDoubleOrNull()
                expiryViewModel.addExpiryItem(name, date, quantityVal, unit.ifBlank { null }) {
                    viewModel.fetchInventory()
                }
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        EditInventoryItemDialog(
            itemName = item.ingredient?.name ?: "Item",
            initialQuantity = item.quantity ?: 0.0,
            initialExpiryDate = item.expiryDate,
            initialUnit = item.unit,
            onDismiss = { editingItem = null },
            onConfirm = { newQty, expiryDate, unit ->
                viewModel.updateInventoryItem(item, newQty, expiryDate, unit)
                editingItem = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPantryItemDialog(
    searchResults: List<com.teamconfused.planmyplate.data.model.IngredientDto>,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (name: String, date: String, quantity: String, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(java.time.LocalDate.now().plusDays(10).toString()) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var expandedUnitDropdown by remember { mutableStateOf(false) }
    val unitOptions = listOf("grams", "kg", "ml", "liters", "pieces", "cups", "tbsp", "tsp")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(date) {
                runCatching {
                    java.time.LocalDate.parse(date)
                        .atStartOfDay(java.time.ZoneId.of("UTC"))
                        .toInstant()
                        .toEpochMilli()
                }.getOrNull()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        date = localDate.toString()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showSearchDialog) {
        IngredientSearchDialog(
            searchResults = searchResults,
            onSearchQueryChange = onSearchQueryChange,
            onSelect = { selectedIng ->
                name = selectedIng.name
                showSearchDialog = false
            },
            onDismiss = {
                showSearchDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to Pantry") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {},
                        label = { Text("Item Name *") },
                        placeholder = { Text("Tap to select ingredient...") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = if (name.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showSearchDialog = true }
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text("Expiry Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    placeholder = { Text("e.g. 2, 500") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expandedUnitDropdown,
                    onExpandedChange = { expandedUnitDropdown = !expandedUnitDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnitDropdown,
                        onDismissRequest = { expandedUnitDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                unit = ""
                                expandedUnitDropdown = false
                            }
                        )
                        unitOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    unit = option
                                    expandedUnitDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && date.isNotBlank()) {
                        onConfirm(name, date, quantity, unit)
                    }
                },
                enabled = name.isNotBlank() && date.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
            
            // Styled Quantity Text
            Text(
                text = "${item.quantity}${if (!item.unit.isNullOrBlank()) " ${item.unit}" else ""}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInventoryItemDialog(
    itemName: String,
    initialQuantity: Double,
    initialExpiryDate: String?,
    initialUnit: String?,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, expiryDate: String?, unit: String?) -> Unit
) {
    var usedText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(initialExpiryDate ?: "") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    val usedAmount = usedText.toDoubleOrNull()
    val isValidUsed = usedText.isEmpty() || (usedAmount != null && usedAmount >= 0.0 && usedAmount <= initialQuantity)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(dateText) {
                runCatching {
                    java.time.LocalDate.parse(dateText)
                        .atStartOfDay(java.time.ZoneId.of("UTC"))
                        .toInstant()
                        .toEpochMilli()
                }.getOrNull()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        dateText = localDate.toString()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Update $itemName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Currently in pantry: $initialQuantity${if (!initialUnit.isNullOrBlank()) " $initialUnit" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = usedText,
                    onValueChange = { usedText = it },
                    label = { Text("Amount Used") },
                    placeholder = { Text("e.g. 1.0, 2") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValidUsed
                )
                if (!isValidUsed) {
                    Text(
                        text = "Enter a valid amount between 0 and $initialQuantity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = {},
                        label = { Text("Expiry Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalUsed = usedText.toDoubleOrNull() ?: 0.0
                    val newQty = (initialQuantity - finalUsed).coerceAtLeast(0.0)
                    onConfirm(newQty, dateText.ifBlank { null }, initialUnit)
                },
                enabled = isValidUsed
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
