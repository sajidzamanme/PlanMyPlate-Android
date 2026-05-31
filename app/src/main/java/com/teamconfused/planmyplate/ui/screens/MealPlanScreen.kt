package com.teamconfused.planmyplate.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import coil.compose.AsyncImage
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.domain.model.AdditionalMeal
import com.teamconfused.planmyplate.domain.model.MealPlan
import com.teamconfused.planmyplate.domain.model.MealSlot
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.model.recipe.MealStyle
import com.teamconfused.planmyplate.domain.model.recipe.mealStyles

import com.teamconfused.planmyplate.ui.theme.PlanMyPlateTheme
import com.teamconfused.planmyplate.ui.viewmodels.MealPlanUiState
import com.teamconfused.planmyplate.ui.viewmodels.MealPlanViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ─── Entry point ───────────────────────────────────────────────────────────

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToRecipeDetails: (recipeId: Int) -> Unit,
    onNavigateToRecipeSelection: (mealType: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) viewModel.refreshAll()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(contentWindowInsets = WindowInsets(0)) { padding ->
        if (uiState.activeMealPlan != null && !uiState.isCreatingPlan) {
            WeeklyMealPlanView(
                mealPlan         = uiState.activeMealPlan!!,
                additionalMeals  = uiState.additionalMeals,
                handledMeals     = uiState.handledMeals,
                modifier         = Modifier.padding(padding),
                onCreateNew      = { viewModel.startNewPlan() },
                onRecipeClick    = { id ->
                    onNavigateToRecipeDetails(id)
                },
            )
        } else {
            CreateMealPlanContent(
                uiState          = uiState,
                onGenerateAi     = {
                    viewModel.generateMealPlan {
                        onNavigateToHome()
                    }
                },
                onUseSelections  = {
                    viewModel.createMealPlan {
                        onNavigateToHome()
                    }
                },
                onMealTypeClick  = { type ->
                    onNavigateToRecipeSelection(type)
                },
                padding          = padding,
            )
        }
    }
}

// ─── Create Plan ──────────────────────────────────────────────────────────

@Composable
private fun CreateMealPlanContent(
    uiState: MealPlanUiState,
    onGenerateAi: () -> Unit,
    onUseSelections: () -> Unit,
    onMealTypeClick: (String) -> Unit,
    padding: PaddingValues,
) {
    val allSelected = uiState.selectedRecipes.values.all { it.size == 7 }

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Top bar ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                Text(
                    text = "Plan your week",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Select 7 recipes for each meal type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // ── Meal type cards ──
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf("Breakfast", "Lunch", "Dinner").forEach { type ->
                    MealTypeRow(
                        mealType  = type,
                        selected  = uiState.selectedRecipes[type] ?: emptyList(),
                        totalSlots = 7,
                        onClick   = { onMealTypeClick(type) },
                    )
                }

                // Error
                uiState.errorMessage?.let { msg ->
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── AI generate button ──
                Button(
                    onClick = onGenerateAi,
                    enabled  = !uiState.isCreatingPlan,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ai_stars),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Generate with AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // ── Manual plan button ──
                OutlinedButton(
                    onClick  = onUseSelections,
                    enabled  = allSelected && !uiState.isCreatingPlan,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        "Use my selections",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                }

                if (!allSelected) {
                    Text(
                        text = "Fill all 7 slots per meal to use your own selections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 2.dp),
                    )
                }
            }
        }

        // ── Generating overlay ──
        if (uiState.isCreatingPlan) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        shape  = RoundedCornerShape(20.dp),
                        color  = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.padding(32.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CircularProgressIndicator(
                                strokeCap = StrokeCap.Round,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "Building your plan…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Meal type row card ────────────────────────────────────────────────────

@Composable
private fun MealTypeRow(
    mealType: String,
    selected: List<Recipe>,
    totalSlots: Int,
    onClick: () -> Unit,
) {
    val style   = mealStyles[mealType]!!
    val filled  = selected.size
    val progress by animateFloatAsState(
        targetValue = filled.toFloat() / totalSlots,
        animationSpec = tween(400),
        label = "progress_$mealType",
    )
    val complete = filled == totalSlots
    val accentAnim by animateColorAsState(
        targetValue = if (complete) style.accent else MaterialTheme.colorScheme.outline,
        label = "accent_$mealType",
    )

    Surface(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        color     = if (complete) style.surface else MaterialTheme.colorScheme.surface,
        border    = BorderStroke(
            width = if (complete) 1.5.dp else 0.5.dp,
            color = if (complete) style.accent.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier  = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Emoji badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = style.surface,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(style.emoji, fontSize = 20.sp)
                        }
                    }
                    Column {
                        Text(
                            mealType,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            if (complete) "All set!" else "$filled / $totalSlots selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (complete) style.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (complete) FontWeight.Medium else FontWeight.Normal,
                        )
                    }
                }
                // Chevron / check
                if (complete) {
                    Surface(shape = CircleShape, color = style.accent.copy(alpha = 0.12f)) {
                        Icon(
                            painter = painterResource(R.drawable.check_icon),
                            contentDescription = "Complete",
                            tint = style.accent,
                            modifier = Modifier.padding(6.dp).size(14.dp),
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(R.drawable.add_icon),
                        contentDescription = "Add $mealType",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress track
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color      = accentAnim,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                strokeCap  = StrokeCap.Round,
            )

            // Recipe thumbnails
            if (selected.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(selected) { recipe ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(52.dp),
                        ) {
                            AsyncImage(
                                model = recipe.imageUrl,
                                contentDescription = recipe.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                recipe.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Weekly plan view ─────────────────────────────────────────────────────

@Composable
fun WeeklyMealPlanView(
    mealPlan: MealPlan,
    additionalMeals: List<AdditionalMeal> = emptyList(),
    handledMeals: Map<String, Set<String>> = emptyMap(),
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
    onCreateNew: () -> Unit,
    onRecipeClick: (Int) -> Unit = {},
) {
    val slots     = mealPlan.slots ?: emptyList()
    val startDate = runCatching { mealPlan.startDate?.let { LocalDate.parse(it) } }.getOrNull()
    val today     = LocalDate.now()

    val groupedByDay = slots.mapIndexed { index, slot ->
        val dayIndex = slot.dayNumber?.takeIf { it > 0 }
            ?: slot.date?.let { d -> startDate?.let { s ->
                runCatching { ChronoUnit.DAYS.between(s, LocalDate.parse(d)).toInt() + 1 }.getOrNull()
            }}?.takeIf { it > 0 }
            ?: ((index / 3) + 1)
        dayIndex to slot
    }.groupBy { it.first }
        .mapValues { it.value.map { p -> p.second } }
        .toSortedMap()

    val todayIndex = startDate?.let { ChronoUnit.DAYS.between(it, today).toInt() + 1 } ?: -1

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Top bar ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text(
                "Weekly plan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                startDate?.let {
                    val end = it.plusDays(6)
                    val fmt = DateTimeFormatter.ofPattern("MMM d")
                    "${it.format(fmt)} – ${end.format(fmt)}"
                } ?: "This week",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // ── Day cards ──
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            (1..7).forEach { dayIndex ->
                val slotsForDay = groupedByDay[dayIndex] ?: emptyList()
                val dateForDay  = startDate?.plusDays(dayIndex.toLong() - 1)
                val isToday     = dayIndex == todayIndex || dateForDay == today
                val displayDate = dateForDay
                    ?.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
                    ?: "Day $dayIndex"

                DayCard(
                    displayDate     = displayDate,
                    isToday         = isToday,
                    slotsForDay     = slotsForDay,
                    dateForDay      = dateForDay,
                    additionalMeals = additionalMeals,
                    handledMeals    = handledMeals,
                    onRecipeClick   = onRecipeClick,
                )
            }

            Spacer(Modifier.height(4.dp))

            OutlinedButton(
                onClick  = onCreateNew,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text(
                    "Replace plan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ─── Day card ─────────────────────────────────────────────────────────────

@Composable
private fun DayCard(
    displayDate: String,
    isToday: Boolean,
    slotsForDay: List<MealSlot>,
    dateForDay: LocalDate?,
    additionalMeals: List<AdditionalMeal>,
    handledMeals: Map<String, Set<String>>,
    onRecipeClick: (Int) -> Unit,
) {
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = if (isToday) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        border = if (isToday) androidx.compose.foundation.BorderStroke(
            1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        ) else androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Day header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    displayDate,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                )
                if (isToday) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Text(
                            "Today",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            if (slotsForDay.isEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "No meals planned",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            } else {
                Spacer(Modifier.height(10.dp))
                listOf("Breakfast", "Lunch", "Dinner").forEach { type ->
                    val slot       = slotsForDay.find { it.mealType.equals(type, ignoreCase = true) }
                    val extras     = additionalMeals.filter {
                        it.date == dateForDay?.toString() && it.mealType.equals(type, ignoreCase = true)
                    }
                    val isHandled  = handledMeals[dateForDay?.toString()]?.contains(type) == true
                    val mealStyle  = mealStyles[type]!!

                    // Planned slot row
                    MealSlotRow(
                        type      = type,
                        style     = mealStyle,
                        recipe    = slot?.recipe,
                        isHandled = isHandled,
                        isToday   = isToday,
                        onRecipeClick = onRecipeClick,
                    )

                    // Extra meals
                    extras.forEach { extra ->
                        ExtraSlotRow(
                            recipe = extra.recipe,
                            style  = mealStyle,
                            onRecipeClick = onRecipeClick,
                        )
                    }
                }
            }
        }
    }
}

// ─── Meal slot row ────────────────────────────────────────────────────────

@Composable
private fun MealSlotRow(
    type: String,
    style: MealStyle,
    recipe: Recipe?,
    isHandled: Boolean,
    isToday: Boolean,
    onRecipeClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Meal type chip
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = style.accent.copy(alpha = 0.10f),
            modifier = Modifier.width(78.dp),
        ) {
            Text(
                text = type,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = style.accent,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1,
            )
        }

        if (recipe != null) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { recipe.recipeId?.let { onRecipeClick(it) } }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Thumbnail with done overlay
                Box(modifier = Modifier.size(40.dp)) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .alpha(if (isHandled) 0.45f else 1f),
                    )
                    if (isHandled) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center).size(18.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check_icon),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(3.dp),
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        recipe.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isHandled) TextDecoration.LineThrough else null,
                    )
                    Text(
                        "${recipe.calories} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    "Not planned",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

// ─── Extra slot row ───────────────────────────────────────────────────────

@Composable
private fun ExtraSlotRow(
    recipe: Recipe,
    style: MealStyle,
    onRecipeClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.width(78.dp),
        ) {
            Text(
                "Extra",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onRecipeClick(recipe.recipeId ?: return@clickable) }
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${recipe.calories} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateMealPlanPreview() {
    val mockRecipe = Recipe(
        recipeId = 1,
        name = "Spicy Chicken Pasta",
        description = "A delicious spicy pasta with chicken.",
        calories = 650,
        imageUrl = null
    )
    val mockUiState = MealPlanUiState(
        selectedRecipes = mapOf(
            "Breakfast" to listOf(mockRecipe, mockRecipe),
            "Lunch" to emptyList(),
            "Dinner" to emptyList()
        )
    )
    PlanMyPlateTheme {
        CreateMealPlanContent(
            uiState = mockUiState,
            onGenerateAi = {},
            onUseSelections = {},
            onMealTypeClick = {},
            padding = PaddingValues(0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyMealPlanPreview() {
    val mockRecipe = Recipe(
        recipeId = 1,
        name = "Spicy Chicken Pasta",
        description = "A delicious spicy pasta with chicken.",
        calories = 650,
        imageUrl = null
    )
    val mockMealPlan = MealPlan(
        mpId = 1,
        startDate = LocalDate.now().toString(),
        duration = 7,
        slots = (1..7).flatMap { day ->
            listOf(
                MealSlot(slotId = day * 3, mealType = "Breakfast", dayNumber = day, recipe = mockRecipe),
                MealSlot(slotId = day * 3 + 1, mealType = "Lunch", dayNumber = day, recipe = mockRecipe),
                MealSlot(slotId = day * 3 + 2, mealType = "Dinner", dayNumber = day, recipe = mockRecipe)
            )
        }
    )
    PlanMyPlateTheme {
        WeeklyMealPlanView(
            mealPlan = mockMealPlan,
            onCreateNew = {}
        )
    }
}
