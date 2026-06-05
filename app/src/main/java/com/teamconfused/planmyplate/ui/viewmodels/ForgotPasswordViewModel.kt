package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.ForgotPasswordRequest
import com.teamconfused.planmyplate.data.model.ResetPasswordRequest
import com.teamconfused.planmyplate.network.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    EMAIL_INPUT,
    VERIFICATION_CODE,
    RESET_PASSWORD,
    SUCCESS
}

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EMAIL_INPUT,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val error: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val resetToken: String? = null
)

class ForgotPasswordViewModel(
    private val authService: AuthService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onCodeChange(code: String) {
        if (code.length <= 100) { // Accept token as string
             _uiState.update { it.copy(code = code, error = null) }
        }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password, error = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, error = null) }
    }

    fun onSendCodeClick() {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(error = "Email is required") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.update { it.copy(error = "Invalid email format") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = ForgotPasswordRequest(email = currentState.email)
                val response = authService.forgotPassword(request)
                _uiState.update { 
                    it.copy(
                        step = ForgotPasswordStep.VERIFICATION_CODE, 
                        error = null,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                Log.e("ForgotPasswordViewModel", "Failed to send reset code: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        errorMessage = e.localizedMessage ?: "Failed to send reset code",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun onVerifyCodeClick() {
        val currentState = _uiState.value
        if (currentState.code.isBlank()) {
            _uiState.update { it.copy(error = "Please enter the reset token") }
            return
        }
        
        // Store the token for the reset password step
        _uiState.update { it.copy(step = ForgotPasswordStep.RESET_PASSWORD, error = null, resetToken = currentState.code) }
    }

    fun onResetPasswordClick() {
        val currentState = _uiState.value
        if (currentState.newPassword.isBlank()) {
            _uiState.update { it.copy(error = "Password is required") }
            return
        }
        if (currentState.newPassword.length < 6) {
             _uiState.update { it.copy(error = "Password must be at least 6 characters") }
             return
        }
        if (currentState.newPassword != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }

        if (currentState.resetToken == null) {
            _uiState.update { it.copy(error = "Reset token is missing") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = ResetPasswordRequest(
                    resetToken = currentState.resetToken,
                    newPassword = currentState.newPassword
                )
                authService.resetPassword(request)
                _uiState.update { it.copy(step = ForgotPasswordStep.SUCCESS, error = null, isLoading = false) }
            } catch (e: Exception) {
                Log.e("ForgotPasswordViewModel", "Failed to reset password: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        errorMessage = e.localizedMessage ?: "Failed to reset password",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
