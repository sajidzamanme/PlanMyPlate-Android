package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.usecase.FilterRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GetAllRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GetRecipeUseCase
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RecipeUiState {
    object Loading : RecipeUiState()
    data class Success(val recipes: List<Recipe>) : RecipeUiState()
    data class Error(val message: String) : RecipeUiState()
}

class RecipeViewModel(
    private val getAllRecipesUseCase: GetAllRecipesUseCase,
    private val filterRecipesUseCase: FilterRecipesUseCase,
    private val getRecipeUseCase: GetRecipeUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _allRecipesState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val allRecipesState: StateFlow<RecipeUiState> = _allRecipesState.asStateFlow()

    private val _recommendedRecipesState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val recommendedRecipesState: StateFlow<RecipeUiState> = _recommendedRecipesState.asStateFlow()

    private val _budgetRecipesState = MutableStateFlow<RecipeUiState>(RecipeUiState.Loading)
    val budgetRecipesState: StateFlow<RecipeUiState> = _budgetRecipesState.asStateFlow()

    private val _selectedRecipeState = MutableStateFlow<Recipe?>(null)
    val selectedRecipeState: StateFlow<Recipe?> = _selectedRecipeState.asStateFlow()

    private val _isDetailsLoading = MutableStateFlow(false)
    val isDetailsLoading: StateFlow<Boolean> = _isDetailsLoading.asStateFlow()

    init {
    }

    fun fetchAllRecipes() {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            _allRecipesState.value = RecipeUiState.Loading
            try {
                val recipes = getAllRecipesUseCase(authHeader)
                _allRecipesState.value = RecipeUiState.Success(recipes)
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to fetch recipes: ${e.message}", e)
                _allRecipesState.value = RecipeUiState.Error(e.message ?: "Failed to fetch recipes")
            }
        }
    }

    fun fetchRecommendedRecipes() {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            _recommendedRecipesState.value = RecipeUiState.Loading
            try {
                val all = getAllRecipesUseCase(authHeader)
                // Simple recommendation logic: take 5 random
                _recommendedRecipesState.value = RecipeUiState.Success(all.shuffled().take(5))
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to fetch recommended recipes: ${e.message}", e)
                 _recommendedRecipesState.value = RecipeUiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun fetchBudgetRecipes() {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
         viewModelScope.launch {
            _budgetRecipesState.value = RecipeUiState.Loading
            try {
                val budget = filterRecipesUseCase.byCalories(authHeader, 0, 400)
                _budgetRecipesState.value = RecipeUiState.Success(budget)
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to fetch budget recipes: ${e.message}", e)
                 _budgetRecipesState.value = RecipeUiState.Error(e.message ?: "Failed")
            }
        }
    }
    
    fun getRecipeById(id: Int) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            _isDetailsLoading.value = true
            try {
                val recipe = getRecipeUseCase(authHeader, id)
                _selectedRecipeState.value = recipe
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to get recipe by ID: ${e.message}", e)
            } finally {
                _isDetailsLoading.value = false
            }
        }
    }

    fun clearSelectedRecipe() {
        _selectedRecipeState.value = null
    }

    fun searchRecipes(query: String) {
        // Implement search using usage case or repository if needed
    }
}
