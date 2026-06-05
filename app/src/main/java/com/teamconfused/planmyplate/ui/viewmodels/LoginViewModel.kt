package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.SigninRequest
import com.teamconfused.planmyplate.network.AuthService
import com.teamconfused.planmyplate.network.UserPreferencesService
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val authService: AuthService,
    private val userPreferencesService: UserPreferencesService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onLoginClick(onLoginSuccess: (hasPreferences: Boolean) -> Unit) {
        val currentState = _uiState.value
        var isValid = true

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
             _uiState.update { it.copy(emailError = "Invalid email format") }
             isValid = false
        }

        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        }

        if (isValid) {
            // Admin bypass
            if (currentState.email == "admin@email.com" && currentState.password == "12345678") {
                sessionManager.saveUserId(0)
                sessionManager.saveAuthToken("admin-bypass-token")
                _uiState.update { it.copy(isLoading = false) }
                onLoginSuccess(false)
                return
            }
            
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    val request = SigninRequest(
                        email = currentState.email,
                        password = currentState.password
                    )
                    val response = authService.signin(request)
                    val userId = response.userId
                    
                    var hasPreferences = false
                    if (userId != null) {
                        sessionManager.saveUserId(userId)
                        response.accessToken?.let { sessionManager.saveAuthToken(it) }
                        // Check if preferences are already set in the database
                        try {
                            val token = response.accessToken ?: ""
                            val authHeader = "Bearer $token"
                            val prefsResponse = userPreferencesService.getPreferences(authHeader, userId)
                            val prefs = prefsResponse.toDomain()
                            // Save preferences locally
                            sessionManager.saveUserPreferences(prefs)
                            
                            // Basic check: if diet or budget are set, we assume preferences exist
                            hasPreferences = prefs.prefId != null || prefs.diet != null || prefs.budget != null
                        } catch (e: Exception) {
                            Log.e("LoginViewModel", "Failed to fetch user preferences: ${e.message}", e)
                            // If it fails (e.g. 404), assume preferences are not set
                            hasPreferences = false
                        }
                    } else {
                        Log.e("LoginViewModel", "Login successful but no userId found in response: $response")
                    }
                    
                    _uiState.update { it.copy(isLoading = false) }
                    onLoginSuccess(hasPreferences)
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Login failed: ${e.message}", e)
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = e.localizedMessage ?: "Login failed. Please try again."
                        ) 
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
