package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.CreateMealPlanRequest
import com.teamconfused.planmyplate.domain.model.AdditionalMeal
import com.teamconfused.planmyplate.domain.model.MealPlan
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.repository.MealPlanRepository
import com.teamconfused.planmyplate.domain.usecase.CreateMealPlanUseCase
import com.teamconfused.planmyplate.domain.usecase.FilterRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GenerateMealPlanUseCase
import com.teamconfused.planmyplate.domain.usecase.GenerateRecipeUseCase
import com.teamconfused.planmyplate.domain.usecase.GetAllRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GetTodaysMealsUseCase
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MealPlanUiState(
    val selectedRecipes: Map<String, List<Recipe>> = mapOf(
        "Breakfast" to emptyList(),
        "Lunch" to emptyList(),
        "Dinner" to emptyList()
    ),
    val servingsMultipliers: Map<String, List<Int>> = mapOf(
        "Breakfast" to List(7) { 1 },
        "Lunch" to List(7) { 1 },
        "Dinner" to List(7) { 1 }
    ),
    val isCreatingPlan: Boolean = false,
    val planCreated: Boolean = false,
    val isLoadingHistory: Boolean = false,
    val activeMealPlan: MealPlan? = null,
    val mealPlans: List<MealPlan> = emptyList(),
    val additionalMeals: List<AdditionalMeal> = emptyList(),
    val handledMeals: Map<String, Set<String>> = emptyMap(),
    val isReplacingPlan: Boolean = false,
    val errorMessage: String? = null
)

class MealPlanViewModel(
    private val createMealPlanUseCase: CreateMealPlanUseCase,
    private val getTodaysMealsUseCase: GetTodaysMealsUseCase,
    private val getAllRecipesUseCase: GetAllRecipesUseCase,
    private val filterRecipesUseCase: FilterRecipesUseCase,
    private val generateRecipeUseCase: GenerateRecipeUseCase,
    private val generateMealPlanUseCase: GenerateMealPlanUseCase,
    private val mealPlanRepository: MealPlanRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    private val _allRecipesState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val allRecipesState: StateFlow<RecipeUiState> = _allRecipesState.asStateFlow()

    private val _recommendedRecipesState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val recommendedRecipesState: StateFlow<RecipeUiState> = _recommendedRecipesState.asStateFlow()

    private val _budgetRecipesState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val budgetRecipesState: StateFlow<RecipeUiState> = _budgetRecipesState.asStateFlow()

    init {
        loadLocalData()
    }
    
    fun loadLocalData() {
        _uiState.update { 
            it.copy(
                additionalMeals = sessionManager.getAdditionalMeals(),
                handledMeals = sessionManager.getHandledMeals()
            )
        }
    }
    
    fun loadRecipes() {
        val token = sessionManager.getAuthToken()
        if (token == null) return

        val authHeader = "Bearer $token"

        viewModelScope.launch {
            _allRecipesState.value = RecipeUiState.Loading
            _recommendedRecipesState.value = RecipeUiState.Loading
            _budgetRecipesState.value = RecipeUiState.Loading
            try {
                val all = getAllRecipesUseCase(authHeader)
                _allRecipesState.value = RecipeUiState.Success(all)
                _recommendedRecipesState.value = RecipeUiState.Success(all.shuffled().take(5))
                _budgetRecipesState.value = RecipeUiState.Success(filterRecipesUseCase.byCalories(authHeader, 0, 400))
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Failed to load recipes: ${e.message}", e)
                _allRecipesState.value = RecipeUiState.Error(com.teamconfused.planmyplate.util.NetworkUtils.parseError(e))
                _recommendedRecipesState.value = RecipeUiState.Error(com.teamconfused.planmyplate.util.NetworkUtils.parseError(e))
                _budgetRecipesState.value = RecipeUiState.Error(com.teamconfused.planmyplate.util.NetworkUtils.parseError(e))
            }
        }
    }
    
    private fun recipesMatch(a: Recipe, b: Recipe): Boolean {
        return (a.recipeId != null && b.recipeId != null && a.recipeId == b.recipeId)
            || (a.recipeId == null && b.recipeId == null && a.name == b.name)
    }

    fun toggleRecipe(mealType: String, recipe: Recipe) {
        if (recipe.recipeId == null) return
        val current = _uiState.value.selectedRecipes[mealType] ?: emptyList()
        val updated = if (current.any { recipesMatch(it, recipe) }) {
            current.filter { !recipesMatch(it, recipe) }
        } else if (current.size < 7) {
            current + recipe
        } else {
            current
        }
        
        _uiState.update { 
            it.copy(selectedRecipes = it.selectedRecipes + (mealType to updated))
        }
    }

    fun updateServingsMultiplier(mealType: String, index: Int, multiplier: Int) {
        _uiState.update { state ->
            val currentList = state.servingsMultipliers[mealType]?.toMutableList() ?: MutableList(7) { 1 }
            if (index in currentList.indices) {
                currentList[index] = multiplier
            }
            state.copy(servingsMultipliers = state.servingsMultipliers + (mealType to currentList))
        }
    }

    fun createMealPlan(onSuccess: () -> Unit) {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        val currentState = _uiState.value
        val breakfast = currentState.selectedRecipes["Breakfast"] ?: emptyList()
        val lunch = currentState.selectedRecipes["Lunch"] ?: emptyList()
        val dinner = currentState.selectedRecipes["Dinner"] ?: emptyList()

        if (breakfast.size != 7 || lunch.size != 7 || dinner.size != 7) {
            _uiState.update { it.copy(errorMessage = "Please select 7 recipes for each meal type") }
            return
        }

        // Validate all selected recipes have non-null IDs
        val invalidRecipes = mutableListOf<String>()
        breakfast.forEachIndexed { idx, r -> if (r.recipeId == null) invalidRecipes.add("Breakfast #${idx + 1}: ${r.name}") }
        lunch.forEachIndexed { idx, r -> if (r.recipeId == null) invalidRecipes.add("Lunch #${idx + 1}: ${r.name}") }
        dinner.forEachIndexed { idx, r -> if (r.recipeId == null) invalidRecipes.add("Dinner #${idx + 1}: ${r.name}") }
        if (invalidRecipes.isNotEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Some recipes are missing IDs. Please re-select:\n${invalidRecipes.joinToString("\n")}")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingPlan = true, errorMessage = null, planCreated = false) }
            try {
                val bMultipliers = currentState.servingsMultipliers["Breakfast"] ?: List(7) { 1 }
                val lMultipliers = currentState.servingsMultipliers["Lunch"] ?: List(7) { 1 }
                val dMultipliers = currentState.servingsMultipliers["Dinner"] ?: List(7) { 1 }

                val recipeIds = mutableListOf<Int>()
                val multipliers = mutableListOf<Int>()
                
                for (i in 0 until 7) {
                    val bId = breakfast[i].recipeId ?: -1
                    val lId = lunch[i].recipeId ?: -1
                    val dId = dinner[i].recipeId ?: -1

                    if (bId == -1 || lId == -1 || dId == -1) {
                        _uiState.update { it.copy(errorMessage = "Error processing recipes — some recipes have invalid IDs.") }
                        return@launch
                    }

                    recipeIds.add(bId)
                    multipliers.add(bMultipliers.getOrElse(i) { 1 })
                    recipeIds.add(lId)
                    multipliers.add(lMultipliers.getOrElse(i) { 1 })
                    recipeIds.add(dId)
                    multipliers.add(dMultipliers.getOrElse(i) { 1 })
                }

                if (recipeIds.size != 21) {
                    _uiState.update { it.copy(errorMessage = "Error processing recipes — expected 21 recipes but got ${recipeIds.size}.") }
                    return@launch
                }

                val token = sessionManager.getAuthToken() ?: ""
                val authHeader = "Bearer $token"
                
                val request = CreateMealPlanRequest(
                    recipeIds = recipeIds,
                    servingsMultipliers = multipliers,
                    duration = 7,
                    startDate = LocalDate.now().toString()
                )
                
                createMealPlanUseCase(authHeader, userId, request)
                
                fetchWeeklyMealPlans()
                sessionManager.setHasMealPlans(true)

                _uiState.update { it.copy(planCreated = true, isReplacingPlan = false) }
                onSuccess()
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Failed to create meal plan: ${e.message}", e)
                _uiState.update { it.copy(errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)) }
            } finally {
                _uiState.update { it.copy(isCreatingPlan = false) }
            }
        }
    }

    fun fetchWeeklyMealPlans() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistory = true) }
            try {
                val token = sessionManager.getAuthToken() ?: ""
                val authHeader = "Bearer $token"
                val plans = mealPlanRepository.getWeeklyMealPlans(authHeader, userId)
                val active = plans.find { it.status.equals("active", ignoreCase = true) }
                _uiState.update {
                    it.copy(
                        isLoadingHistory = false,
                        mealPlans = plans,
                        activeMealPlan = if (it.isReplacingPlan) it.activeMealPlan else active
                    )
                }
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Failed to fetch weekly meal plans: ${e.message}", e)
                _uiState.update { it.copy(isLoadingHistory = false) }
            }
        }
    }

    fun retryFetchRecipes() {
        loadRecipes()
    }
    
    fun refreshAll() {
        loadLocalData()
        fetchWeeklyMealPlans()
        loadRecipes()
    }

    fun refreshRecipes() {
        loadRecipes()
    }

    fun startNewPlan() {
        _uiState.update {
            it.copy(
                activeMealPlan = null,
                isCreatingPlan = false,
                isReplacingPlan = true,
                selectedRecipes = mapOf(
                    "Breakfast" to emptyList(),
                    "Lunch" to emptyList(),
                    "Dinner" to emptyList()
                ),
                servingsMultipliers = mapOf(
                    "Breakfast" to List(7) { 1 },
                    "Lunch" to List(7) { 1 },
                    "Dinner" to List(7) { 1 }
                )
            )
        }
    }
    
    fun generateMealPlan(onSuccess: () -> Unit) {
        val userId = sessionManager.getUserId()
        val token = sessionManager.getAuthToken()
        
        if (userId == -1 || token == null) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingPlan = true, errorMessage = null, planCreated = false) }
            try {
                // Call AI UseCase
                generateMealPlanUseCase("Bearer $token", userId)
                
                fetchWeeklyMealPlans() // Refresh list
                sessionManager.setHasMealPlans(true)
                
                _uiState.update { it.copy(planCreated = true, isReplacingPlan = false) }
                onSuccess()
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Failed to generate meal plan: ${e.message}", e)
                _uiState.update { 
                    it.copy(errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)) 
                }
            } finally {
                _uiState.update { it.copy(isCreatingPlan = false) }
            }
        }
    }
}
