package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.UserPreferencesRequest
import com.teamconfused.planmyplate.domain.repository.UserPreferencesRepository
import com.teamconfused.planmyplate.network.IngredientService
import com.teamconfused.planmyplate.util.NetworkUtils
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreferenceSelectionUiState(
    val currentStep: Int = 0,
    val selectedDiets: Set<String> = emptySet(),
    val selectedAllergies: Set<String> = emptySet(),
    val selectedDislikes: Set<String> = emptySet(),
    val selectedBudget: Float = 50F,
    val selectedHeight: String = "",
    val selectedWeight: String = "",
    val selectedGender: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableDiets: List<String> = emptyList(),
    val availableIngredients: List<String> = emptyList()
)

class PreferenceSelectionViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val ingredientService: IngredientService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(PreferenceSelectionUiState())
    val uiState: StateFlow<PreferenceSelectionUiState> = _uiState.asStateFlow()

    init {
        loadReferenceData()
        loadExistingPreferences()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Parallel fetch
                val diets = userPreferencesRepository.getDiets().mapNotNull { it.dietName }
                val ingredients = ingredientService.getAllIngredients().mapNotNull { it.name }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        availableDiets = diets,
                        availableIngredients = ingredients
                    ) 
                }
            } catch (e: Exception) {
                Log.e("PreferenceSelectionViewModel", "Failed to load reference data: ${e.message}", e)
                 _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = NetworkUtils.parseError(e)
                    ) 
                }
            }
        }
    }

    fun onDietSelected(diet: String) {
        _uiState.update {
            val current = it.selectedDiets
            if (current.contains(diet)) {
                it.copy(selectedDiets = current - diet)
            } else {
                it.copy(selectedDiets = current + diet)
            }
        }
    }

    fun onAllergyToggled(allergy: String) {
        _uiState.update {
            val current = it.selectedAllergies
            if (current.contains(allergy)) {
                it.copy(selectedAllergies = current - allergy)
            } else {
                it.copy(selectedAllergies = current + allergy)
            }
        }
    }

    fun onDislikeToggled(dislike: String) {
        _uiState.update {
            val current = it.selectedDislikes
            if (current.contains(dislike)) {
                it.copy(selectedDislikes = current - dislike)
            } else {
                it.copy(selectedDislikes = current + dislike)
            }
        }
    }

    fun onGenderSelected(gender: String?) {
        _uiState.update { it.copy(selectedGender = gender) }
    }

    fun onHeightChanged(height: String) {
        _uiState.update { it.copy(selectedHeight = height) }
    }

    fun onWeightChanged(weight: String) {
        _uiState.update { it.copy(selectedWeight = weight) }
    }

    fun onBudgetSelected(budget: Float) {
        _uiState.update { it.copy(selectedBudget = budget) }
    }

    fun onNextStep(onComplete: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.currentStep < 4) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        } else {
            savePreferences(onComplete)
        }
    }

    private fun loadExistingPreferences() {
        val userId = sessionManager.getUserId()
        if (userId == -1 || userId == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                val response = userPreferencesRepository.getPreferences(authHeader, userId)
                sessionManager.saveUserPreferences(response)
                
                _uiState.update { it.copy(
                    selectedDiets = response.diets?.toSet() ?: emptySet(),
                    selectedAllergies = response.allergies?.toSet() ?: emptySet(),
                    selectedDislikes = response.dislikes?.toSet() ?: emptySet(),
                    selectedBudget = response.budget ?: 50f,
                    selectedHeight = response.height?.toString() ?: "",
                    selectedWeight = response.weight?.toString() ?: "",
                    selectedGender = response.gender,
                    isLoading = false
                )}
            } catch (e: Exception) {
                Log.e("PreferenceSelectionViewModel", "Failed to load existing preferences: ${e.message}", e)
                // If it's a 404, we just stop loading; it's okay for new users
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun savePreferences(onComplete: () -> Unit) {
        val currentState = _uiState.value
        val userId = sessionManager.getUserId()
        
        if (userId == -1) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }
        
        // Admin bypass - skip server call
        if (userId == 0) {
            _uiState.update { it.copy(isLoading = false) }
            onComplete()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Convert sets to lists to match the backend's expectation
                val allergiesList = if (currentState.selectedAllergies.isEmpty()) null 
                                   else currentState.selectedAllergies.toList()
                val dislikesList = if (currentState.selectedDislikes.isEmpty()) null 
                                  else currentState.selectedDislikes.toList()

                val request = UserPreferencesRequest(
                    userId = userId,
                    diets = currentState.selectedDiets.toList(),
                    allergies = allergiesList,
                    dislikes = dislikesList,
                    budget = currentState.selectedBudget,
                    height = currentState.selectedHeight.toFloatOrNull(),
                    weight = currentState.selectedWeight.toFloatOrNull(),
                    gender = currentState.selectedGender
                )
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                val savedPrefs = userPreferencesRepository.setPreferences(authHeader, userId, request)
                sessionManager.saveUserPreferences(savedPrefs)
                _uiState.update { it.copy(isLoading = false) }
                onComplete()
            } catch (e: Exception) {
                Log.e("PreferenceSelectionViewModel", "Failed to save preferences: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)
                    ) 
                }
            }
        }
    }

    suspend fun isPreferencesSet(id: Int): Boolean {
        return try {
            val token = sessionManager.getAuthToken() ?: return false
            val authHeader = "Bearer $token"
            val response = userPreferencesRepository.getPreferences(authHeader, id)
            // Check if the returned response has actual data.
            response.prefId != null || !response.diets.isNullOrEmpty() || response.budget != null
        } catch (e: Exception) {
            Log.e("PreferenceSelectionViewModel", "Failed to check if preferences are set: ${e.message}", e)
            // If 404 is thrown, it usually means preferences don't exist
            false
        }
    }

    fun onPreviousStep(onBack: () -> Unit) {
        _uiState.update {
            if (it.currentStep > 0) {
                it.copy(currentStep = it.currentStep - 1)
            } else {
                onBack()
                it
            }
        }
    }
}
