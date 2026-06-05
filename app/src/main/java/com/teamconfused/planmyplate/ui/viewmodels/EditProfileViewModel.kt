package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.UpdateUserRequest
import com.teamconfused.planmyplate.data.model.UserDto
import com.teamconfused.planmyplate.network.UserService
import com.teamconfused.planmyplate.util.NetworkUtils
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val firstName: String = "",
    val firstNameError: String? = null,
    val lastName: String = "",
    val lastNameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val email: String = "",
    val dateOfBirth: String = "",
    val dateOfBirthError: String? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class EditProfileViewModel(
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val token = sessionManager.getAuthToken()
        if (token == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = userService.getCurrentUser("Bearer $token")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        firstName = user.firstName ?: "",
                        lastName = user.lastName ?: "",
                        phone = user.phone ?: "",
                        email = user.email ?: "",
                        dateOfBirth = user.dateOfBirth ?: ""
                    )
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Failed to load current user", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = NetworkUtils.parseError(e)
                    )
                }
            }
        }
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, firstNameError = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, lastNameError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, phoneError = null) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value, dateOfBirthError = null) }
    }

    fun updateProfile(onSuccess: () -> Unit) {
        val token = sessionManager.getAuthToken()
        val userId = sessionManager.getUserId()
        if (token == null || userId == -1) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        val state = _uiState.value
        var isValid = true

        if (state.firstName.isBlank()) {
            _uiState.update { it.copy(firstNameError = "First name is required") }
            isValid = false
        }
        if (state.lastName.isBlank()) {
            _uiState.update { it.copy(lastNameError = "Last name is required") }
            isValid = false
        }
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(phoneError = "Phone number is required") }
            isValid = false
        }
        if (state.dateOfBirth.isBlank()) {
            _uiState.update { it.copy(dateOfBirthError = "Date of birth is required") }
            isValid = false
        }

        if (!isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = UpdateUserRequest(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    phone = state.phone,
                    dateOfBirth = state.dateOfBirth
                )
                userService.updateUser("Bearer $token", userId, request)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Failed to update profile", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = NetworkUtils.parseError(e)
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
