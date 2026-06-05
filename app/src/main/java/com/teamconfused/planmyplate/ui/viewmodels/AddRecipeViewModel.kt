package com.teamconfused.planmyplate.ui.viewmodels

import android.content.Context
import android.util.Log
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.CreateRecipeRequest
import com.teamconfused.planmyplate.data.model.RecipeIngredientRequest
import com.teamconfused.planmyplate.network.RecipeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

data class RecipeIngredientInput(
    val ingId: Int = 0,
    val ingredientName: String = "",
    val quantity: String = "",
    val unit: String = ""
)

data class AddRecipeUiState(
    val name: String = "",
    val description: String = "",
    val calories: String = "",
    val prepTime: String = "",
    val cookTime: String = "",
    val servings: String = "",
    val instructions: String = "",
    val imageUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val ingredients: List<RecipeIngredientInput> = listOf(RecipeIngredientInput()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AddRecipeViewModel(
    private val recipeService: RecipeService,
    private val ingredientService: com.teamconfused.planmyplate.network.IngredientService,
    private val sessionManager: com.teamconfused.planmyplate.util.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.teamconfused.planmyplate.data.model.IngredientDto>>(emptyList())
    val searchResults: StateFlow<List<com.teamconfused.planmyplate.data.model.IngredientDto>> = _searchResults.asStateFlow()

    fun searchIngredients(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val results = ingredientService.searchIngredientsByName(query)
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e("AddRecipeViewModel", "Search failed: ${e.message}", e)
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateCalories(calories: String) {
        _uiState.update { it.copy(calories = calories) }
    }

    fun updatePrepTime(prepTime: String) {
        _uiState.update { it.copy(prepTime = prepTime) }
    }

    fun updateCookTime(cookTime: String) {
        _uiState.update { it.copy(cookTime = cookTime) }
    }

    fun updateServings(servings: String) {
        _uiState.update { it.copy(servings = servings) }
    }

    fun updateInstructions(instructions: String) {
        _uiState.update { it.copy(instructions = instructions) }
    }

    fun updateImageUrl(imageUrl: String?) {
        _uiState.update { it.copy(imageUrl = imageUrl) }
    }

    fun uploadImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true, errorMessage = null) }
            try {
                val file = getFileFromUri(context, uri)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val token = sessionManager.getAuthToken() ?: ""
                    val authHeader = "Bearer $token"
                    val response = recipeService.uploadImage(authHeader, body)
                    _uiState.update { it.copy(imageUrl = response.url, isUploadingImage = false) }
                } else {
                    _uiState.update { it.copy(isUploadingImage = false, errorMessage = "Failed to process image") }
                }
            } catch (e: Exception) {
                Log.e("AddRecipeViewModel", "Image upload failed: ${e.message}", e)
                _uiState.update { it.copy(isUploadingImage = false, errorMessage = "Upload failed: ${e.message}") }
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "upload_${System.currentTimeMillis()}.jpg"
            val tempFile = File(context.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("AddRecipeViewModel", "Failed to get file from URI: ${e.message}", e)
            null
        }
    }

    fun addIngredient() {
        _uiState.update {
            it.copy(ingredients = it.ingredients + RecipeIngredientInput())
        }
    }

    fun removeIngredient(index: Int) {
        _uiState.update {
            it.copy(ingredients = it.ingredients.filterIndexed { i, _ -> i != index })
        }
    }

    fun updateIngredient(index: Int, ingredient: RecipeIngredientInput) {
        _uiState.update {
            val updated = it.ingredients.toMutableList()
            if (index in updated.indices) {
                updated[index] = ingredient
            }
            it.copy(ingredients = updated)
        }
    }

    fun createRecipe(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validation
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Recipe name is required") }
            return
        }

        val ingredientRequests = state.ingredients
            .filter { it.ingId > 0 && it.quantity.isNotBlank() }
            .map {
                RecipeIngredientRequest(
                    ingId = it.ingId,
                    quantity = it.quantity.toIntOrNull() ?: 1,
                    unit = it.unit.ifBlank { "unit" }
                )
            }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val request = CreateRecipeRequest(
                    name = state.name,
                    description = state.description.ifBlank { null },
                    calories = state.calories.toIntOrNull(),
                    prepTime = state.prepTime.toIntOrNull(),
                    cookTime = state.cookTime.toIntOrNull(),
                    servings = state.servings.toIntOrNull(),
                    instructions = state.instructions.ifBlank { null },
                    imageUrl = state.imageUrl,
                    ingredients = ingredientRequests.ifEmpty { null }
                )

                val token = sessionManager.getAuthToken() ?: ""
                val authHeader = "Bearer $token"
                recipeService.createRecipe(authHeader, request)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Recipe created successfully!"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("AddRecipeViewModel", "Failed to create recipe: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to create recipe: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
