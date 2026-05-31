package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.ui.components.HorizontalRecipeCard
import com.teamconfused.planmyplate.ui.viewmodels.HomeViewModel
import com.teamconfused.planmyplate.util.SessionManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToMealPlan: () -> Unit,
    onNavigateToRecipeDetails: (recipeId: Int, fromDashboard: Boolean, mealType: String?) -> Unit
) {
    val context = LocalContext.current
    val sessionManager: SessionManager = koinInject()
    val hasMealPlans = sessionManager.hasMealPlans()
    
    val uiState by viewModel.uiState.collectAsState()
    
    var recipeToShowDetails by remember { mutableStateOf<Recipe?>(null) }
    var mealTypeToShowDetails by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Initial load on first composition
    LaunchedEffect(Unit) {
        viewModel.fetchTodaysMeals()
    }
    
    // Sync refresh state with loading state
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }
    
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatedRecipe by viewModel.generatedRecipe.collectAsState()
    var showGenerateDialog by remember { mutableStateOf(false) }
    var generatingMealType by remember { mutableStateOf<String?>("Dinner") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showGenerateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.teamconfused.planmyplate.R.drawable.ic_ai_stars),
                    contentDescription = "AI Actions"
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchTodaysMeals()
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            if (hasMealPlans) {
                DashboardWithMeals(
                    uiState = uiState, 
                    onRetry = { viewModel.retry() },
                    onRecipeClick = { recipe, type -> 
                        recipeToShowDetails = recipe 
                        mealTypeToShowDetails = type
                    },
                    onNavigateToMealPlan = onNavigateToMealPlan
                )
            } else {
                EmptyDashboard(onNavigateToMealPlan = onNavigateToMealPlan)
            }
        }
    }

    if (showGenerateDialog) {
        RecipeGenerationDialog(
            onDismiss = { showGenerateDialog = false },
            onGenerate = { ingredients, mood ->
                val type = "Dinner" 
                generatingMealType = type
                viewModel.generateRecipe(ingredients, type, mapOf("mood" to mood))
                showGenerateDialog = false
            }
        )
    }
    
    // Loading State for Generation
    if (isGenerating) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Working Magic...") },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Our AI chef is cooking up a recipe for you!")
                }
            },
            confirmButton = {}
        )
    }

    // Show Generated Recipe
    generatedRecipe?.let { recipe ->
        LaunchedEffect(recipe.recipeId) {
            onNavigateToRecipeDetails(
                recipe.recipeId ?: 0,
                true,
                generatingMealType
            )
            viewModel.clearGeneratedRecipe()
        }
    }

    recipeToShowDetails?.let { recipe ->
        LaunchedEffect(recipe.recipeId) {
            onNavigateToRecipeDetails(
                recipe.recipeId ?: 0,
                true,
                mealTypeToShowDetails
            )
            recipeToShowDetails = null
            mealTypeToShowDetails = null
        }
    }
}

@Composable
fun RecipeGenerationDialog(
    onDismiss: () -> Unit,
    onGenerate: (List<String>, String) -> Unit
) {
    var ingredientsText by remember { mutableStateOf("") }
    var moodText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Recipe with AI") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    label = { Text("Available Ingredients (comma separated)") },
                    placeholder = { Text("e.g. chicken, rice, tomato") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = moodText,
                    onValueChange = { moodText = it },
                    label = { Text("Mood / Occasion") },
                    placeholder = { Text("e.g. Quick Dinner, Romantic") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ingredients = ingredientsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onGenerate(ingredients, moodText)
                }
            ) {
                Text("Generate")
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
fun DashboardWithMeals(
    uiState: com.teamconfused.planmyplate.ui.viewmodels.HomeUiState,
    onRetry: () -> Unit,
    onRecipeClick: (Recipe, String?) -> Unit,
    onNavigateToMealPlan: () -> Unit
) {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (uiState.errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error Loading Meals",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            FilledTonalButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Retry")
            }
        }
        return
    }
    
    val todayCalories = uiState.todayCalories
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = currentDate.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Consumed",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.consumedCalories}",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Planned",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$todayCalories",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        Text(
            text = "Today's Meals",
            style = MaterialTheme.typography.headlineSmall
        )
        
        val today = java.time.LocalDate.now().toString()
        
        fun getAdditionalForType(type: String) = uiState.additionalMeals
            .filter { it.date == today && it.mealType.equalsIgnoreCase(type) }
            .map { it.recipe }

        if (!uiState.handledMealTypes.contains("Breakfast")) {
            val planned = uiState.todayBreakfast
            val additional = getAdditionalForType("Breakfast")
            if (planned != null || additional.isNotEmpty()) {
                MealSection("Breakfast", planned, additional) { onRecipeClick(it, "Breakfast") }
            }
        }
        if (!uiState.handledMealTypes.contains("Lunch")) {
            val planned = uiState.todayLunch
            val additional = getAdditionalForType("Lunch")
            if (planned != null || additional.isNotEmpty()) {
                MealSection("Lunch", planned, additional) { onRecipeClick(it, "Lunch") }
            }
        }
        if (!uiState.handledMealTypes.contains("Dinner")) {
            val planned = uiState.todayDinner
            val additional = getAdditionalForType("Dinner")
            if (planned != null || additional.isNotEmpty()) {
                MealSection("Dinner", planned, additional) { onRecipeClick(it, "Dinner") }
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
        )

        
        Text(
            text = "Upcoming Meals",
            style = MaterialTheme.typography.headlineSmall
        )
        
        if (uiState.upcomingMeals.isNotEmpty()) {
            UpcomingMealSection(uiState.upcomingDayLabel ?: "Upcoming", uiState.upcomingMeals) { onRecipeClick(it, null) }
        } else if (uiState.upcomingMessage != null) {
            Text(
                text = uiState.upcomingMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        FilledTonalButton(
            onClick = onNavigateToMealPlan,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View Full Meal Plan")
        }
    }
}

@Composable
fun MealSection(
    mealType: String,
    plannedRecipe: Recipe?,
    additionalRecipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = mealType,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        plannedRecipe?.let { recipe ->
            HorizontalRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
        }
        
        additionalRecipes.forEach { recipe ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Additional",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
            }
        }
    }
}

// Helper extension
fun String.equalsIgnoreCase(other: String) = this.equals(other, ignoreCase = true)

@Composable
fun UpcomingMealSection(label: String, recipes: List<Recipe>, onRecipeClick: (Recipe) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        recipes.forEach { recipe ->
            HorizontalRecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
        }
    }
}

@Composable
fun EmptyDashboard(onNavigateToMealPlan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No Meal Plans Yet",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Create your first meal plan to see your daily meals here",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        FilledTonalButton(
            onClick = onNavigateToMealPlan,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Create Meal Plan")
        }
    }
}
