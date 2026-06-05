package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.domain.model.AdditionalMeal
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.usecase.GenerateRecipeUseCase
import com.teamconfused.planmyplate.domain.usecase.GetTodaysMealsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayBreakfast: Recipe? = null,
    val todayLunch: Recipe? = null,
    val todayDinner: Recipe? = null,
    val upcomingMeals: List<Recipe> = emptyList(),
    val upcomingDayLabel: String? = null,
    val upcomingMessage: String? = null,
    val errorMessage: String? = null,
    val consumedCalories: Int = 0,
    val cookedMeals: Set<String> = emptySet(),
    val skippedMeals: Set<String> = emptySet(),
    val additionalMeals: List<AdditionalMeal> = emptyList()
) {
    val todayCalories: Int
        get() = (todayBreakfast?.calories ?: 0) + 
                (todayLunch?.calories ?: 0) + 
                (todayDinner?.calories ?: 0) +
                additionalMeals.sumOf { it.recipe.calories }

    val handledMealTypes: Set<String>
        get() = cookedMeals + skippedMeals
}

class HomeViewModel(
    private val getTodaysMealsUseCase: GetTodaysMealsUseCase,
    private val generateRecipeUseCase: GenerateRecipeUseCase,
    private val sessionManager: com.teamconfused.planmyplate.util.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadLocalData()
    }

    private fun loadLocalData() {
        val today = java.time.LocalDate.now().toString()
        val cooked = sessionManager.getCookedMeals()[today] ?: emptySet()
        val skipped = sessionManager.getSkippedMeals()[today] ?: emptySet()
        val calories = sessionManager.getConsumedCalories()[today] ?: 0
        val additional = sessionManager.getAdditionalMeals().filter { it.date == today }
        
        _uiState.update { 
            it.copy(
                cookedMeals = cooked,
                skippedMeals = skipped,
                consumedCalories = calories,
                additionalMeals = additional
            )
        }
    }

    fun fetchTodaysMeals() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
             _uiState.update { it.copy(isLoading = false, errorMessage = "User not logged in") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: ""
                val authHeader = "Bearer $token"
                // UseCase handles all logic
                val result = getTodaysMealsUseCase(authHeader, userId)
                
                if (result.hasActivePlan) {
                     sessionManager.setHasMealPlans(true)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayBreakfast = result.todayMeals.breakfast,
                        todayLunch = result.todayMeals.lunch,
                        todayDinner = result.todayMeals.dinner,
                        upcomingMeals = result.upcomingMeals,
                        upcomingDayLabel = result.upcomingDayLabel,
                        upcomingMessage = result.upcomingMessage
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to fetch today's meals: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to fetch meals"
                    )
                }
            }
        }
    }

    fun retry() {
        fetchTodaysMeals()
    }

    // AI Generation
    private val _generatedRecipe = MutableStateFlow<Recipe?>(null)
    val generatedRecipe: StateFlow<Recipe?> = _generatedRecipe.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun generateRecipe(
        ingredients: List<String>,
        mealType: String,
        otherParams: Map<String, Any>
    ) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val token = sessionManager.getAuthToken() ?: ""
                
                val diet = sessionManager.getUserPreferences().diet
                val mood = otherParams["mood"] as? String

                Log.d("HomeViewModel", "Starting AI Recipe generation for type: $mealType with mood: $mood")
                val recipe = generateRecipeUseCase(
                    token = "Bearer $token",
                    ingredients = ingredients, 
                    mood = mood, 
                    dietaryPreference = diet, 
                    maxCalories = 800
                )
                
                Log.d("HomeViewModel", "AI Recipe generated successfully: ${recipe.name}")
                
                val today = java.time.LocalDate.now().toString()
                val newAdditionalMeal = AdditionalMeal(
                    recipeId = recipe.recipeId ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                    recipe = recipe,
                    date = today,
                    mealType = mealType
                )
                
                val allAdditional = sessionManager.getAdditionalMeals() + newAdditionalMeal
                sessionManager.saveAdditionalMeals(allAdditional)
                
                _uiState.update { 
                    it.copy(additionalMeals = it.additionalMeals + newAdditionalMeal)
                }
                
                _generatedRecipe.value = recipe
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in AI generation flow: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun markAsCooked(mealType: String?, calories: Int, recipeId: Int? = null) {
        val today = java.time.LocalDate.now().toString()
        if (mealType == null) return

        _uiState.update { state ->
            if (state.cookedMeals.contains(mealType)) {
                return@update state // already cooked
            }

            val wasSkipped = state.skippedMeals.contains(mealType)
            val newCooked = state.cookedMeals + mealType
            val newSkipped = if (wasSkipped) state.skippedMeals - mealType else state.skippedMeals

            // Persist cooked
            val allCooked = sessionManager.getCookedMeals().toMutableMap()
            allCooked[today] = newCooked
            sessionManager.saveCookedMeals(allCooked)

            // Persist skipped
            val allSkipped = sessionManager.getSkippedMeals().toMutableMap()
            allSkipped[today] = newSkipped
            sessionManager.saveSkippedMeals(allSkipped)

            // Persist handled
            val allHandled = sessionManager.getHandledMeals().toMutableMap()
            allHandled[today] = newCooked + newSkipped
            sessionManager.saveHandledMeals(allHandled)

            // Persist calories
            val allCalories = sessionManager.getConsumedCalories().toMutableMap()
            allCalories[today] = (allCalories[today] ?: 0) + calories
            sessionManager.saveConsumedCalories(allCalories)

            // Handle additional meals
            val newAdditionalInState = if (recipeId != null) state.additionalMeals.filter { r -> r.recipeId != recipeId } else state.additionalMeals
            if (recipeId != null) {
                val allAdditional = sessionManager.getAdditionalMeals().filter { r -> 
                    !(r.recipeId == recipeId && r.date == today)
                }
                sessionManager.saveAdditionalMeals(allAdditional)
            }

            state.copy(
                consumedCalories = state.consumedCalories + calories,
                cookedMeals = newCooked,
                skippedMeals = newSkipped,
                additionalMeals = newAdditionalInState
            )
        }
    }

    fun skipMeal(mealType: String?, recipeId: Int? = null) {
        val today = java.time.LocalDate.now().toString()
        if (mealType == null) return

        _uiState.update { state ->
            if (state.skippedMeals.contains(mealType)) {
                return@update state // already skipped
            }

            val wasCooked = state.cookedMeals.contains(mealType)
            val newSkipped = state.skippedMeals + mealType
            val newCooked = if (wasCooked) state.cookedMeals - mealType else state.cookedMeals

            // Persist cooked
            val allCooked = sessionManager.getCookedMeals().toMutableMap()
            allCooked[today] = newCooked
            sessionManager.saveCookedMeals(allCooked)

            // Persist skipped
            val allSkipped = sessionManager.getSkippedMeals().toMutableMap()
            allSkipped[today] = newSkipped
            sessionManager.saveSkippedMeals(allSkipped)

            // Persist handled
            val allHandled = sessionManager.getHandledMeals().toMutableMap()
            allHandled[today] = newCooked + newSkipped
            sessionManager.saveHandledMeals(allHandled)

            // Persist calories (subtract if was cooked previously)
            val caloriesToSubtract = if (wasCooked) {
                val recipe = when (mealType) {
                    "Breakfast" -> state.todayBreakfast
                    "Lunch" -> state.todayLunch
                    "Dinner" -> state.todayDinner
                    else -> state.additionalMeals.find { it.recipeId == recipeId }?.recipe
                }
                recipe?.calories ?: 0
            } else 0

            if (caloriesToSubtract > 0) {
                val allCalories = sessionManager.getConsumedCalories().toMutableMap()
                allCalories[today] = maxOf(0, (allCalories[today] ?: 0) - caloriesToSubtract)
                sessionManager.saveConsumedCalories(allCalories)
            }

            // Handle additional meals
            val newAdditionalInState = if (recipeId != null) state.additionalMeals.filter { r -> r.recipeId != recipeId } else state.additionalMeals
            if (recipeId != null) {
                val allAdditional = sessionManager.getAdditionalMeals().filter { r -> 
                    !(r.recipeId == recipeId && r.date == today)
                }
                sessionManager.saveAdditionalMeals(allAdditional)
            }

            state.copy(
                consumedCalories = maxOf(0, state.consumedCalories - caloriesToSubtract),
                cookedMeals = newCooked,
                skippedMeals = newSkipped,
                additionalMeals = newAdditionalInState
            )
        }
    }

    fun clearGeneratedRecipe() {
        _generatedRecipe.value = null
    }
}
