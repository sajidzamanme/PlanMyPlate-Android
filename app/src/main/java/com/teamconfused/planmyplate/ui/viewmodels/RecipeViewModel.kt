package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.RecipeRatingRequest
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.model.RecipeRating
import com.teamconfused.planmyplate.domain.model.RecipeRatingSummary
import com.teamconfused.planmyplate.domain.model.UserFavorite
import com.teamconfused.planmyplate.domain.repository.FavoriteRepository
import com.teamconfused.planmyplate.domain.repository.RatingRepository
import com.teamconfused.planmyplate.domain.repository.RecipeRepository
import com.teamconfused.planmyplate.domain.usecase.FilterRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GetAllRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GetRecipeUseCase
import com.teamconfused.planmyplate.util.NetworkUtils
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

sealed class CookRecipeUiState {
    object Idle : CookRecipeUiState()
    object Loading : CookRecipeUiState()
    object Success : CookRecipeUiState()
    data class InsufficientIngredients(
        val title: String,
        val message: String,
        val missing: List<com.teamconfused.planmyplate.data.model.MissingIngredientDto>
    ) : CookRecipeUiState()
    data class Error(val message: String) : CookRecipeUiState()
}

class RecipeViewModel(
    private val getAllRecipesUseCase: GetAllRecipesUseCase,
    private val filterRecipesUseCase: FilterRecipesUseCase,
    private val getRecipeUseCase: GetRecipeUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val ratingRepository: RatingRepository,
    private val recipeRepository: RecipeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
    }

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

    // Favourites & Ratings Flows
    private val _favoritesState = MutableStateFlow<List<UserFavorite>>(emptyList())
    val favoritesState: StateFlow<List<UserFavorite>> = _favoritesState.asStateFlow()

    private val _isFavoriteState = MutableStateFlow(false)
    val isFavoriteState: StateFlow<Boolean> = _isFavoriteState.asStateFlow()

    private val _myRatingState = MutableStateFlow<RecipeRating?>(null)
    val myRatingState: StateFlow<RecipeRating?> = _myRatingState.asStateFlow()

    private val _ratingSummaryState = MutableStateFlow<RecipeRatingSummary?>(null)
    val ratingSummaryState: StateFlow<RecipeRatingSummary?> = _ratingSummaryState.asStateFlow()

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
                _allRecipesState.value = RecipeUiState.Error(NetworkUtils.parseError(e))
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
                _recommendedRecipesState.value = RecipeUiState.Success(all.shuffled().take(5))
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to fetch recommended recipes: ${e.message}", e)
                 _recommendedRecipesState.value = RecipeUiState.Error(NetworkUtils.parseError(e))
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
                 _budgetRecipesState.value = RecipeUiState.Error(NetworkUtils.parseError(e))
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
        _myRatingState.value = null
        _ratingSummaryState.value = null
        _isFavoriteState.value = false
    }

    fun searchRecipes(query: String) {
    }

    // --- Favorites Actions ---

    fun fetchFavorites() {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            try {
                val list = favoriteRepository.getFavorites(authHeader)
                _favoritesState.value = list
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to fetch favorites: ${e.message}", e)
            }
        }
    }

    fun addFavorite(recipeId: Int) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            try {
                favoriteRepository.addFavorite(authHeader, recipeId)
                _isFavoriteState.value = true
                fetchFavorites()
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to add favorite: ${e.message}", e)
            }
        }
    }

    fun removeFavorite(recipeId: Int) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            try {
                favoriteRepository.removeFavorite(authHeader, recipeId)
                _isFavoriteState.value = false
                fetchFavorites()
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to remove favorite: ${e.message}", e)
            }
        }
    }

    fun checkFavoriteStatus(recipeId: Int) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            try {
                val isFav = favoriteRepository.isFavorite(authHeader, recipeId)
                _isFavoriteState.value = isFav
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to check favorite status: ${e.message}", e)
            }
        }
    }

    // --- Ratings Actions ---

    fun rateRecipe(recipeId: Int, rating: Int, review: String?) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            try {
                val request = RecipeRatingRequest(recipeId = recipeId, rating = rating, review = review)
                val response = ratingRepository.rateRecipe(authHeader, request)
                _myRatingState.value = response
                fetchRatingSummary(recipeId)
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to rate recipe: ${e.message}", e)
            }
        }
    }

    fun fetchRatingSummary(recipeId: Int) {
        viewModelScope.launch {
            try {
                val summary = ratingRepository.getRecipeRatingSummary(recipeId)
                _ratingSummaryState.value = summary
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to fetch rating summary: ${e.message}", e)
            }
        }
    }

    fun fetchMyRating(recipeId: Int) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            try {
                val rating = ratingRepository.getMyRatingForRecipe(authHeader, recipeId)
                _myRatingState.value = rating
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to fetch my rating: ${e.message}", e)
                _myRatingState.value = null
            }
        }
    }

    fun deleteRating(recipeId: Int) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            try {
                ratingRepository.deleteMyRating(authHeader, recipeId)
                _myRatingState.value = null
                fetchRatingSummary(recipeId)
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to delete rating: ${e.message}", e)
            }
        }
    }

    private val _cookRecipeState = MutableStateFlow<CookRecipeUiState>(CookRecipeUiState.Idle)
    val cookRecipeState: StateFlow<CookRecipeUiState> = _cookRecipeState.asStateFlow()

    fun resetCookRecipeState() {
        _cookRecipeState.value = CookRecipeUiState.Idle
    }

    fun cookRecipe(recipeId: Int, servings: Float? = null, force: Boolean? = null) {
        val token = sessionManager.getAuthToken() ?: return
        val authHeader = "Bearer $token"
        viewModelScope.launch {
            _cookRecipeState.value = CookRecipeUiState.Loading
            try {
                recipeRepository.cookRecipe(authHeader, recipeId, servings, force)
                _cookRecipeState.value = CookRecipeUiState.Success
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 409) {
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        if (errorBody != null) {
                            val errorResponse = json.decodeFromString(com.teamconfused.planmyplate.data.model.CookRecipeErrorResponse.serializer(), errorBody)
                            
                            if (errorResponse.status == "insufficient_ingredients") {
                                _cookRecipeState.value = CookRecipeUiState.InsufficientIngredients(
                                    title = errorResponse.title,
                                    message = errorResponse.message,
                                    missing = errorResponse.missing
                                )
                                return@launch
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e("RecipeViewModel", "Failed to parse 409 error response", ex)
                    }
                }
                Log.e("RecipeViewModel", "Failed to cook recipe: ${e.message}", e)
                _cookRecipeState.value = CookRecipeUiState.Error(NetworkUtils.parseError(e))
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to cook recipe: ${e.message}", e)
                _cookRecipeState.value = CookRecipeUiState.Error(NetworkUtils.parseError(e))
            }
        }
    }
}
