package com.teamconfused.planmyplate.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.model.recipe.MealStyle
import com.teamconfused.planmyplate.domain.model.recipe.mealStyles
import com.teamconfused.planmyplate.ui.viewmodels.MealPlanViewModel
import com.teamconfused.planmyplate.ui.viewmodels.RecipeUiState

// ─── Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSelectionScreen(
    mealType: String,
    viewModel: MealPlanViewModel,
    onBackClick: () -> Unit,
    onRecipeDetailsClick: (Int) -> Unit,
) {
    val uiState          by viewModel.uiState.collectAsState()
    val allRecipesState  by viewModel.allRecipesState.collectAsState()
    val recommendedState by viewModel.recommendedRecipesState.collectAsState()
    val budgetState      by viewModel.budgetRecipesState.collectAsState()

    val selectedRecipes  = uiState.selectedRecipes[mealType] ?: emptyList()
    val style            = mealStyles[mealType] ?: MealStyle("🍽️", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surface)
    val accent           = style.accent

    var searchQuery  by rememberSaveable { mutableStateOf("") }
    var activeFilter by rememberSaveable { mutableStateOf("All") }
    val keyboard     = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        val s = viewModel.allRecipesState.value
        if (s is RecipeUiState.Loading || s is RecipeUiState.Error) viewModel.refreshRecipes()
    }

    fun RecipeUiState.recipes() = (this as? RecipeUiState.Success)?.recipes?.filter { it.recipeId != null } ?: emptyList()

    val baseRecipes = when (activeFilter) {
        "Recommended" -> recommendedState.recipes()
        "Budget"      -> budgetState.recipes()
        else          -> allRecipesState.recipes()
    }
    val filteredRecipes = if (searchQuery.isBlank()) baseRecipes
    else baseRecipes.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val currentState = when (activeFilter) {
        "Recommended" -> recommendedState
        "Budget"      -> budgetState
        else          -> allRecipesState
    }

    val progress by animateFloatAsState(
        targetValue = selectedRecipes.size / 7f,
        animationSpec = tween(300),
        label = "progress",
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back_icon),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "${ style.emoji }  $mealType",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Choose 7 recipes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    actions = {
                        // Counter pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedRecipes.size == 7) accent.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 16.dp),
                        ) {
                            Text(
                                text = "${selectedRecipes.size} / 7",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedRecipes.size == 7) accent
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                // Slim progress strip under top bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color      = accent,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    strokeCap  = StrokeCap.Butt,
                )
            }
        },
        bottomBar = {
            BottomBar(
                selected = selectedRecipes.size,
                accent   = accent,
                onDone   = onBackClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Search ──
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = {
                    Text("Search recipes…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search_icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                painter = painterResource(R.drawable.clear_icon),
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = accent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            )

            // ── Filter chips ──
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                val filters = listOf("All", "Recommended", "Budget")
                items(filters.size) { i ->
                    val f = filters[i]
                    val active = activeFilter == f
                    FilterChip(
                        selected = active,
                        onClick  = { activeFilter = f },
                        label    = {
                            Text(
                                f,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent.copy(alpha = 0.12f),
                            selectedLabelColor     = accent,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = active,
                            borderColor         = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = accent,
                            borderWidth         = 0.5.dp,
                            selectedBorderWidth = 1.dp,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }

            // ── Selected strip ──
            if (selectedRecipes.isNotEmpty()) {
                SelectedStrip(recipes = selectedRecipes, accent = accent, onRemove = { viewModel.toggleRecipe(mealType, it) })
            }

            // ── Grid ──
            RecipeGrid(
                state           = currentState,
                recipes         = filteredRecipes,
                selected        = selectedRecipes,
                accent          = accent,
                searchQuery     = searchQuery,
                onToggle        = { viewModel.toggleRecipe(mealType, it) },
                onDetailsClick  = onRecipeDetailsClick,
                onRetry         = { viewModel.refreshRecipes() },
            )
        }
    }
}

// ─── Bottom bar ───────────────────────────────────────────────────────────

@Composable
private fun BottomBar(selected: Int, accent: Color, onDone: () -> Unit) {
    Surface(
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier       = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = if (selected == 7) "All set!" else "$selected of 7 selected",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected == 7) accent else MaterialTheme.colorScheme.onSurface,
                )
                if (selected < 7) {
                    Text(
                        text = "${7 - selected} more to go",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(
                onClick = onDone,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = accent),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    "Done",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─── Selected strip ───────────────────────────────────────────────────────

@Composable
private fun SelectedStrip(
    recipes: List<Recipe>,
    accent: Color,
    onRemove: (Recipe) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "Selected",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
            Surface(shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
                Text(
                    "${recipes.size}",
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 10.dp),
        ) {
            items(recipes.size) { i ->
                val r = recipes[i]
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = accent.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, accent.copy(alpha = 0.3f)),
                ) {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        AsyncImage(
                            model = r.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Text(
                            r.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 72.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Icon(
                            painter = painterResource(R.drawable.remove_icon),
                            contentDescription = "Remove",
                            tint = accent,
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .combinedClickable(onClick = { onRemove(r) }),
                        )
                    }
                }
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

// ─── Recipe grid ─────────────────────────────────────────────────────────

@Composable
private fun RecipeGrid(
    state: RecipeUiState,
    recipes: List<Recipe>,
    selected: List<Recipe>,
    accent: Color,
    searchQuery: String,
    onToggle: (Recipe) -> Unit,
    onDetailsClick: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is RecipeUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent, strokeCap = StrokeCap.Round)
            }
        }
        is RecipeUiState.Error -> {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Couldn't load recipes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    state.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(10.dp)) {
                    Text("Retry")
                }
            }
        }
        is RecipeUiState.Success -> {
            if (recipes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (searchQuery.isNotBlank()) "No results for \"$searchQuery\""
                            else "No recipes available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = recipes,
                        key   = { it.recipeId ?: it.name.hashCode() },
                    ) { recipe ->
                        val isSelected = selected.any { s ->
                            (s.recipeId != null && recipe.recipeId != null && s.recipeId == recipe.recipeId)
                                    || (s.recipeId == null && recipe.recipeId == null && s.name == recipe.name)
                        }
                        RecipeCard(
                            recipe         = recipe,
                            isSelected     = isSelected,
                            accent         = accent,
                            onToggle       = { onToggle(recipe) },
                            onDetailsClick = { recipe.recipeId?.let { onDetailsClick(it) } },
                        )
                    }
                }
            }
        }
    }
}

// ─── Recipe card ──────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    isSelected: Boolean,
    accent: Color,
    onToggle: () -> Unit,
    onDetailsClick: () -> Unit,
) {
    val borderWidth by animateFloatAsState(
        targetValue = if (isSelected) 1.5f else 0.5f,
        animationSpec = tween(200),
        label = "border",
    )

    Surface(
        shape  = RoundedCornerShape(14.dp),
        color  = if (isSelected) accent.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = borderWidth.dp,
            color = if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggle, onLongClick = onDetailsClick),
    ) {
        Column {
            // ── Image ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f),
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop,
                )

                // Calorie badge — bottom left
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(7.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                ) {
                    Text(
                        "${recipe.calories} kcal",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Prep time — top left
                if ((recipe.prepTime ?: 0) > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(7.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.45f),
                    ) {
                        Text(
                            "${recipe.prepTime}m",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                }

                // Check mark — top right
                if (isSelected) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(7.dp)
                            .size(26.dp),
                        shape = CircleShape,
                        color = accent,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check_icon),
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                }
            }

            // ── Text ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (recipe.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}