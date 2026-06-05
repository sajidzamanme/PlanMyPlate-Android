package com.teamconfused.planmyplate.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import coil.compose.AsyncImage
import com.teamconfused.planmyplate.ui.viewmodels.AddRecipeViewModel
import com.teamconfused.planmyplate.ui.viewmodels.RecipeIngredientInput
import com.teamconfused.planmyplate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: AddRecipeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var activeSearchIndex by remember { mutableStateOf<Int?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.uploadImage(context, it) }
        }
    )

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(R.drawable.arrow_back_icon), "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Selection Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { 
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.imageUrl != null) {
                        AsyncImage(
                            model = uiState.imageUrl,
                            contentDescription = "Recipe Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add_icon),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Add Recipe Image",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (uiState.isUploadingImage) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            // Basic Info Section
            Text("Basic Information", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Recipe Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.calories,
                    onValueChange = { viewModel.updateCalories(it) },
                    label = { Text("Calories") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.servings,
                    onValueChange = { viewModel.updateServings(it) },
                    label = { Text("Servings") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.prepTime,
                    onValueChange = { viewModel.updatePrepTime(it) },
                    label = { Text("Prep Time (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.cookTime,
                    onValueChange = { viewModel.updateCookTime(it) },
                    label = { Text("Cook Time (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Divider()

            // Instructions Section
            Text("Instructions", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = uiState.instructions,
                onValueChange = { viewModel.updateInstructions(it) },
                label = { Text("Cooking Instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                placeholder = { Text("1. Step one\n2. Step two\n...") }
            )

            Divider()

            // Ingredients Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.addIngredient() }) {
                    Icon(painter = painterResource(R.drawable.add_icon), "Add Ingredient")
                }
            }

            uiState.ingredients.forEachIndexed { index, ingredient ->
                IngredientInputCard(
                    ingredient = ingredient,
                    onUpdate = { viewModel.updateIngredient(index, it) },
                    onRemove = { viewModel.removeIngredient(index) },
                    onSearchClick = { activeSearchIndex = index },
                    canRemove = uiState.ingredients.size > 1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error/Success Messages
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Submit Button
            Button(
                onClick = { viewModel.createRecipe { onNavigateBack() } },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.name.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Recipe")
                }
            }
        }
    }

    if (activeSearchIndex != null) {
        IngredientSearchDialog(
            searchResults = searchResults,
            onSearchQueryChange = { viewModel.searchIngredients(it) },
            onSelect = { selectedIng ->
                activeSearchIndex?.let { index ->
                    val currentIng = uiState.ingredients[index]
                    viewModel.updateIngredient(
                        index,
                        currentIng.copy(
                            ingId = selectedIng.ingId ?: 0,
                            ingredientName = selectedIng.name
                        )
                    )
                }
                activeSearchIndex = null
            },
            onDismiss = {
                activeSearchIndex = null
            }
        )
    }
}

@Composable
fun IngredientInputCard(
    ingredient: RecipeIngredientInput,
    onUpdate: (RecipeIngredientInput) -> Unit,
    onRemove: () -> Unit,
    onSearchClick: () -> Unit,
    canRemove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ingredient", style = MaterialTheme.typography.labelMedium)
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(painter = painterResource(R.drawable.delete_icon), "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearchClick() }
            ) {
                OutlinedTextField(
                    value = if (ingredient.ingId > 0) ingredient.ingredientName else "",
                    onValueChange = {},
                    label = { Text("Ingredient Name *") },
                    placeholder = { Text("Tap to search database...") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = if (ingredient.ingId > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ingredient.quantity,
                    onValueChange = { onUpdate(ingredient.copy(quantity = it)) },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("200") }
                )

                OutlinedTextField(
                    value = ingredient.unit,
                    onValueChange = { onUpdate(ingredient.copy(unit = it)) },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("grams") }
                )
            }

            Text(
                text = "Custom recipe ingredients are restricted to verified database ingredients.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IngredientSearchDialog(
    searchResults: List<com.teamconfused.planmyplate.data.model.IngredientDto>,
    onSearchQueryChange: (String) -> Unit,
    onSelect: (com.teamconfused.planmyplate.data.model.IngredientDto) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    
    LaunchedEffect(query) {
        onSearchQueryChange(query)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Verified Ingredient") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Type ingredient name...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (query.isBlank()) "Type to start searching" else "No matching ingredients found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults) { ingredient ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(ingredient) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ingredient.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
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
