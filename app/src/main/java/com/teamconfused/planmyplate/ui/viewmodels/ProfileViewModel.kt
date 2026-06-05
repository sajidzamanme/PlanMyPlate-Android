package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.UserDto
import com.teamconfused.planmyplate.domain.model.UserPreferences
import com.teamconfused.planmyplate.domain.repository.UserPreferencesRepository
import com.teamconfused.planmyplate.network.UserService
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val preferences: UserPreferences? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userService: UserService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        val userId = sessionManager.getUserId()
        val token = sessionManager.getAuthToken()

        if (userId == -1 || token == null) {
            _uiState.update { it.copy(errorMessage = "User session not found") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val authHeader = "Bearer $token"
                val userResponse = userService.getCurrentUser(authHeader)
                val preferencesResponse = try {
                    userPreferencesRepository.getPreferences(authHeader, userId)
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Failed to fetch preferences: ${e.message}")
                    null
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = userResponse,
                        preferences = preferencesResponse
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to load profile data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load profile details"
                    )
                }
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
