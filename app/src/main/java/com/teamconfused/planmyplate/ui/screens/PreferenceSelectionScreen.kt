package com.teamconfused.planmyplate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.ui.theme.PlanMyPlateTheme
import com.teamconfused.planmyplate.ui.viewmodels.PreferenceSelectionUiState
import kotlin.math.roundToInt

@Composable
fun PreferenceSelectionScreen(
    uiState: PreferenceSelectionUiState,
    onDietSelected: (String) -> Unit,
    onAllergyToggled: (String) -> Unit,
    onDislikeToggled: (String) -> Unit,
    onGenderSelected: (String?) -> Unit,
    onHeightChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onBudgetSelected: (Float) -> Unit,
    onNextStep: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            PreferenceTopBar(
                currentStep = uiState.currentStep,
                totalSteps = 5,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.navigationBarsPadding()
            ) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = onNextStep,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.currentStep == 4) "Finish" else "Continue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        LaunchedEffect(uiState.currentStep) {
            scrollState.scrollTo(0)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = getTitleForStep(uiState.currentStep),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (uiState.currentStep) {
                0 -> DietSelectionStep(
                    availableDiets = uiState.availableDiets,
                    selectedDiet = uiState.selectedDiet,
                    onDietSelected = onDietSelected
                )
                1 -> MultiSelectStep(
                    options = uiState.availableIngredients,
                    selectedOptions = uiState.selectedAllergies,
                    onOptionToggled = onAllergyToggled
                )
                2 -> MultiSelectStep(
                    options = uiState.availableIngredients,
                    selectedOptions = uiState.selectedDislikes,
                    onOptionToggled = onDislikeToggled
                )
                3 -> PhysicalInfoStep(
                    selectedGender = uiState.selectedGender,
                    onGenderSelected = onGenderSelected,
                    height = uiState.selectedHeight,
                    onHeightChanged = onHeightChanged,
                    weight = uiState.selectedWeight,
                    onWeightChanged = onWeightChanged
                )
                4 -> BudgetSelectionStep(
                    selectedBudget = uiState.selectedBudget,
                    onBudgetSelected = onBudgetSelected
                )
            }
            
            // Add some extra space at bottom for scrolling above the button
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceTopBar(
    currentStep: Int,
    totalSteps: Int,
    onBackClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_icon),
                        contentDescription = "Back"
                    )
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.5f
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun DietSelectionStep(
    availableDiets: List<String>,
    selectedDiet: String?,
    onDietSelected: (String) -> Unit
) {
    // diets list removed, using availableDiets parameter

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        availableDiets.forEach { diet ->
            SelectionButton(
                text = diet,
                isSelected = diet == selectedDiet,
                onClick = { onDietSelected(diet) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectStep(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionToggled: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            SelectionChip(
                text = option,
                isSelected = selectedOptions.contains(option),
                onClick = { onOptionToggled(option) }
            )
        }
    }
}

@Composable
fun GenderSelectionStep(
    selectedGender: String?,
    onGenderSelected: (String?) -> Unit
) {
    val options = listOf("male", "female", "other")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { gender ->
            SelectionButton(
                text = gender.replaceFirstChar { it.uppercase() },
                isSelected = gender == selectedGender,
                onClick = { onGenderSelected(gender) }
            )
        }
    }
}

@Composable
fun HeightInputStep(
    height: String,
    onHeightChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Enter your height (optional)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = height,
            onValueChange = onHeightChanged,
            label = { Text("Height (cm)") },
            placeholder = { Text("e.g. 175") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun WeightInputStep(
    weight: String,
    onWeightChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Enter your weight (optional)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChanged,
            label = { Text("Weight (kg)") },
            placeholder = { Text("e.g. 70") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun BudgetSelectionStep(
    selectedBudget: Float,
    onBudgetSelected: (Float) -> Unit
) {
    val valueRange = 50f..1000f
    val fraction = (selectedBudget - valueRange.start) /
            (valueRange.endInclusive - valueRange.start)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {

            Slider(
                value = selectedBudget,
                onValueChange = {
                    val snapped = (it / 50f).roundToInt() * 50f
                    onBudgetSelected(snapped.coerceIn(valueRange))
                },
                valueRange = valueRange,
                steps = 0,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    thumbColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(
                        x = androidx.compose.ui.unit.lerp(
                            start = 0.dp,
                            stop = 280.dp, // approx slider width minus padding
                            fraction = fraction.coerceIn(0f, 1f)
                        ),
                        y = 44.dp
                    )
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "৳${selectedBudget.toInt()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Drag to adjust your budget",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreferenceSelection_BudgetStep_Preview() {
    PlanMyPlateTheme {
        PreferenceSelectionScreen(
            uiState = PreferenceSelectionUiState(
                currentStep = 4,
                availableDiets = listOf("Classic", "Vegetarian"),
                availableIngredients = listOf("Peanuts", "Shellfish", "Mushrooms"),
                selectedDiet = "Classic",
                selectedAllergies = emptySet(),
                selectedDislikes = emptySet(),
                selectedHeight = "175",
                selectedWeight = "70",
                selectedGender = "male"
            ),
            onDietSelected = {},
            onAllergyToggled = {},
            onDislikeToggled = {},
            onGenderSelected = {},
            onHeightChanged = {},
            onWeightChanged = {},
            onBudgetSelected = {},
            onNextStep = {},
            onBackClick = {}
        )
    }
}

@Composable
fun SelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun SelectionChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun PhysicalInfoStep(
    selectedGender: String?,
    onGenderSelected: (String?) -> Unit,
    height: String,
    onHeightChanged: (String) -> Unit,
    weight: String,
    onWeightChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Gender",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val genders = listOf("male", "female", "other")
                genders.forEach { gender ->
                    val isSelected = gender == selectedGender
                    Surface(
                        onClick = { onGenderSelected(gender) },
                        shape = RoundedCornerShape(12.dp),
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gender.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Height (optional)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = onHeightChanged,
                    placeholder = { Text("cm") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Weight (optional)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = onWeightChanged,
                    placeholder = { Text("kg") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    }
}

private fun getTitleForStep(step: Int): String {
    return when (step) {
        0 -> "Pick your diet"
        1 -> "Any allergies?"
        2 -> "How about dislikes?"
        3 -> "Physical Details"
        4 -> "Daily Budget?"
        else -> ""
    }
}
