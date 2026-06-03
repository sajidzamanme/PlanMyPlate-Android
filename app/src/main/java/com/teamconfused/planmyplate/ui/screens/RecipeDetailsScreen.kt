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

    var isAdded by remember { mutableStateOf(isInitiallyAdded) }

    LaunchedEffect(recipeId) {
        viewModel.getRecipeById(recipeId)
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
                if (fromDashboard) {
                    Surface(
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
                                    onCooked(mealType, currentRecipe.calories ?: 0, currentRecipe.recipeId)
                                    onNavigateBack()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Text(
                                    "Cooked",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (showControls) {
                    Surface(
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
            }
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