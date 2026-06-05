package com.teamconfused.planmyplate.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.ui.viewmodels.RecipeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsScreen(
    recipeId: Int,
    isInitiallyAdded: Boolean = false,
    showControls: Boolean = true,
    fromDashboard: Boolean = false,
    mealType: String? = null,
    onNavigateBack: () -> Unit,
    onToggleRecipe: (Recipe) -> Unit = {},
    onCooked: (String?, Int, Int?) -> Unit = { _, _, _ -> },
    onSkip: (Int?) -> Unit = {}
) {
    val viewModel: RecipeViewModel = koinViewModel()
    val recipe by viewModel.selectedRecipeState.collectAsState()
    val isLoading by viewModel.isDetailsLoading.collectAsState()

    val isFavorite by viewModel.isFavoriteState.collectAsState()
    val myRating by viewModel.myRatingState.collectAsState()
    val ratingSummary by viewModel.ratingSummaryState.collectAsState()

    val cookRecipeState by viewModel.cookRecipeState.collectAsState()

    var showMissingIngredientsDialog by remember { mutableStateOf<com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.InsufficientIngredients?>(null) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cookRecipeState) {
        when (cookRecipeState) {
            is com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.Success -> {
                onCooked(mealType, recipe?.calories ?: 0, recipe?.recipeId)
                viewModel.resetCookRecipeState()
                onNavigateBack()
            }
            is com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.InsufficientIngredients -> {
                showMissingIngredientsDialog = cookRecipeState as com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.InsufficientIngredients
            }
            is com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.Error -> {
                showErrorDialog = (cookRecipeState as com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.Error).message
            }
            else -> {}
        }
    }

    showMissingIngredientsDialog?.let { data ->
        AlertDialog(
            onDismissRequest = { 
                showMissingIngredientsDialog = null 
                viewModel.resetCookRecipeState()
            },
            title = { 
                Text(
                    text = data.title.ifBlank { "Missing Pantry Items" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "You do not have enough ingredients in your pantry. Would you like to force cook anyway?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Missing Items:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    data.missing.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${item.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Need ${item.required}${item.unit} (Have ${item.available}${item.unit})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val missingData = showMissingIngredientsDialog
                        showMissingIngredientsDialog = null
                        if (missingData != null) {
                            viewModel.cookRecipe(recipeId, force = true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Force Cook")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showMissingIngredientsDialog = null 
                        viewModel.resetCookRecipeState()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    showErrorDialog?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { 
                showErrorDialog = null 
                viewModel.resetCookRecipeState()
            },
            title = { Text("Error Cooking Recipe") },
            text = { Text(errorMsg) },
            confirmButton = {
                Button(onClick = { 
                    showErrorDialog = null 
                    viewModel.resetCookRecipeState()
                }) {
                    Text("OK")
                }
            }
        )
    }

    var isAdded by remember { mutableStateOf(isInitiallyAdded) }

    LaunchedEffect(recipeId) {
        viewModel.getRecipeById(recipeId)
        viewModel.checkFavoriteStatus(recipeId)
        viewModel.fetchRatingSummary(recipeId)
        viewModel.fetchMyRating(recipeId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    recipe?.let { currentRecipe ->
        Scaffold(
            bottomBar = {
                if (fromDashboard && mealType != null) {
                    Surface(
                        modifier = Modifier.navigationBarsPadding(),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onSkip(currentRecipe.recipeId)
                                    onNavigateBack()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Text("Skip")
                            }

                            Button(
                                onClick = {
                                    viewModel.cookRecipe(currentRecipe.recipeId ?: 0)
                                },
                                enabled = cookRecipeState !is com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.Loading,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                if (cookRecipeState is com.teamconfused.planmyplate.ui.viewmodels.CookRecipeUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Cooked",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else if (!fromDashboard && showControls) {
                    Surface(
                        modifier = Modifier.navigationBarsPadding(),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Text("Back")
                            }

                            Button(
                                onClick = {
                                    onToggleRecipe(currentRecipe)
                                    isAdded = !isAdded
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAdded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(if (isAdded) "Remove" else "Add")
                            }
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = padding.calculateBottomPadding())
                ) {
                    // Header with Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        AsyncImage(
                            model = currentRecipe.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.8f)
                                        ),
                                        startY = 500f
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            Text(
                                text = currentRecipe.name,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${currentRecipe.calories} kcal",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Quick Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(
                                "Prep",
                                "${currentRecipe.prepTime ?: "-"}\nmin",
                                MaterialTheme.colorScheme.primaryContainer
                            )
                            StatItem(
                                "Cook",
                                "${currentRecipe.cookTime ?: "-"}\nmin",
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                            StatItem(
                                "Serves",
                                "${currentRecipe.servings ?: "-"}\npers.",
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        }

                        // Macros Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Nutritional Information",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    MacroItem("Protein", "${currentRecipe.protein ?: 0.0}g")
                                    MacroItem("Carbs", "${currentRecipe.carbs ?: 0.0}g")
                                    MacroItem("Fat", "${currentRecipe.fat ?: 0.0}g")
                                    MacroItem("Fiber", "${currentRecipe.fiber ?: 0.0}g")
                                }
                            }
                        }

                        // Tab Selection (Pill shape)
                        var selectedTab by remember { mutableIntStateOf(0) }
                        val tabs = listOf("Description", "Ingredients", "Instructions")

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(CircleShape)
                                            .background(if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { selectedTab = index }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selectedTab == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Tab Content
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "TabContent"
                        ) { targetIndex ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                when (targetIndex) {
                                    0 -> DescriptionTab(currentRecipe.description)
                                    1 -> IngredientsTab(currentRecipe.ingredients)
                                    2 -> InstructionsTab(currentRecipe.instructions)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Ratings Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Ratings & Reviews",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            // Summary Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val avg = ratingSummary?.averageRating ?: 0.0
                                    Text(
                                        text = String.format("%.1f", avg),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "out of 5",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${ratingSummary?.totalRatings ?: 0} ratings",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row {
                                        val avg = ratingSummary?.averageRating ?: 0.0
                                        for (i in 1..5) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_ai_stars),
                                                contentDescription = null,
                                                tint = if (i <= avg) Color(0xFFFFB300) else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // User Rating Selector / Review Form
                            var userRating by remember(myRating) { mutableIntStateOf(myRating?.rating ?: 0) }
                            var reviewText by remember(myRating) { mutableStateOf(myRating?.review ?: "") }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = if (myRating == null) "Rate this Recipe" else "Your Rating",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 1..5) {
                                            IconButton(
                                                onClick = { userRating = i },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_ai_stars),
                                                    contentDescription = "$i Stars",
                                                    tint = if (i <= userRating) Color(0xFFFFB300) else Color.LightGray,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = reviewText,
                                        onValueChange = { reviewText = it },
                                        label = { Text("Write a review (optional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (myRating != null) {
                                            TextButton(
                                                onClick = {
                                                    viewModel.deleteRating(recipeId)
                                                },
                                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Delete")
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.weight(1f))
                                        
                                        Button(
                                            onClick = {
                                                if (userRating > 0) {
                                                    viewModel.rateRecipe(recipeId, userRating, reviewText.ifBlank { null })
                                                }
                                            },
                                            enabled = userRating > 0,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(if (myRating == null) "Submit" else "Update")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back_icon),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            currentRecipe.recipeId?.let { id ->
                                if (isFavorite) {
                                    viewModel.removeFavorite(id)
                                } else {
                                    viewModel.addFavorite(id)
                                }
                            }
                        },
                        modifier = Modifier.background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_heart),
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, containerColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun DescriptionTab(description: String) {
    Text(
        text = if (description.isNotBlank()) description else "No description available.",
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 28.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun IngredientsTab(ingredients: List<String>?) {
    if (ingredients.isNullOrEmpty()) {
        Text("No ingredients listed.")
    } else {
        ingredients.forEach { ingredient ->
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(8.dp).padding(top = 8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = ingredient,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun InstructionsTab(instructions: String?) {
    if (instructions.isNullOrBlank()) {
        Text("No instructions available.")
    } else {
        Text(
            text = instructions,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MacroItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}