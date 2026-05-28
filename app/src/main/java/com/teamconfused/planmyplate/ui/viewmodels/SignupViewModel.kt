package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.SignupRequest
import com.teamconfused.planmyplate.network.AuthService
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignupUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val dateOfBirth: String = "1998-01-01",
    val isTermsAccepted: Boolean = false,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SignupViewModel(
    private val authService: AuthService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(name: String) {
        _uiState.update { it.copy(firstName = name, firstNameError = null) }
    }

    fun onLastNameChange(name: String) {
        _uiState.update { it.copy(lastName = name, lastNameError = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }

    fun onDateOfBirthChange(dob: String) {
        _uiState.update { it.copy(dateOfBirth = dob) }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { it.copy(isTermsAccepted = accepted, termsError = null) }
    }

    fun onSignupClick(onSignupSuccess: () -> Unit) {
        val currentState = _uiState.value
        var isValid = true

        if (currentState.firstName.isBlank()) {
            _uiState.update { it.copy(firstNameError = "First Name is required") }
            isValid = false
        }
        
        if (currentState.lastName.isBlank()) {
            _uiState.update { it.copy(lastNameError = "Last Name is required") }
            isValid = false
        }

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
        } else if (currentState.password.length < 8) {
            _uiState.update { it.copy(passwordError = "Password must be at least 8 characters") }
            isValid = false
        }

        if (currentState.phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Phone is required") }
            isValid = false
        }

        if (!currentState.isTermsAccepted) {
            _uiState.update { it.copy(termsError = "You must accept the terms") }
            isValid = false
        }

        if (isValid) {
            // Admin bypass
            if (currentState.firstName == "admin" && currentState.email == "admin@email.com" && currentState.password == "12345678") {
                sessionManager.saveUserId(0)
                sessionManager.saveAuthToken("admin-bypass-token")
                _uiState.update { it.copy(isLoading = false) }
                onSignupSuccess()
                return
            }
            
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    val request = SignupRequest(
                        firstName = currentState.firstName,
                        lastName = currentState.lastName,
                        email = currentState.email,
                        password = currentState.password,
                        phone = currentState.phone,
                        dateOfBirth = currentState.dateOfBirth
                    )
                    val response = authService.signup(request)
                    val userId = response.userId
                    if (userId != null) {
                        sessionManager.saveUserId(userId)
                        response.accessToken?.let { sessionManager.saveAuthToken(it) }
                    } else {
                        Log.e("SignupViewModel", "Signup successful but no userId found in response: $response")
                    }
                    _uiState.update { it.copy(isLoading = false) }
                    onSignupSuccess()
                } catch (e: Exception) {
                    Log.e("SignupViewModel", "Signup failed: ${e.message}", e)
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = e.localizedMessage ?: "Signup failed. Please try again."
                        ) 
                    }
                }
            }
        }
    }
}
