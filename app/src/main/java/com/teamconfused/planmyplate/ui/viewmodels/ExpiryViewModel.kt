package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.ExpiryItemRequest
import com.teamconfused.planmyplate.data.model.UpdateExpiryRequest
import com.teamconfused.planmyplate.domain.model.ExpiryItem
import com.teamconfused.planmyplate.domain.model.SoonToExpireResult
import com.teamconfused.planmyplate.domain.repository.ExpiryRepository
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpiryUiState(
    val expiryItems: List<ExpiryItem> = emptyList(),
    val soonToExpire: SoonToExpireResult? = null,
    val warningDays: Int = 10,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ExpiryViewModel(
    private val expiryRepository: ExpiryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpiryUiState(warningDays = sessionManager.getExpiryWarningDays()))
    val uiState: StateFlow<ExpiryUiState> = _uiState.asStateFlow()

    init {
        fetchSoonToExpireItems()
        fetchExpiryItems()
    }

    fun fetchExpiryItems() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                val items = expiryRepository.listExpiryItems(authHeader, userId)
                _uiState.update { it.copy(isLoading = false, expiryItems = items) }
            } catch (e: Exception) {
                Log.e("ExpiryViewModel", "Failed to fetch expiry items: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)) }
            }
        }
    }

    fun fetchSoonToExpireItems() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                val days = _uiState.value.warningDays
                val result = expiryRepository.getSoonToExpireItems(authHeader, userId, days)
                _uiState.update { it.copy(isLoading = false, soonToExpire = result) }
            } catch (e: Exception) {
                Log.e("ExpiryViewModel", "Failed to fetch soon to expire items: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)) }
            }
        }
    }

    fun addExpiryItem(productName: String, expiryDate: String, quantity: Double?, unit: String?, onSuccess: () -> Unit = {}) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                val request = ExpiryItemRequest(
                    productName = productName,
                    expiryDate = expiryDate,
                    quantity = quantity,
                    unit = unit
                )
                expiryRepository.addExpiryItem(authHeader, userId, request)
                _uiState.update { it.copy(isLoading = false) }
                fetchSoonToExpireItems()
                fetchExpiryItems()
                onSuccess()
            } catch (e: Exception) {
                Log.e("ExpiryViewModel", "Failed to add expiry item: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)) }
            }
        }
    }

    fun updateExpiryWarningDays(days: Int) {
        sessionManager.saveExpiryWarningDays(days)
        _uiState.update { it.copy(warningDays = days) }
        fetchSoonToExpireItems()
    }

    fun deleteExpiryItem(itemId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                expiryRepository.deleteExpiryItem(authHeader, itemId)
                _uiState.update { it.copy(isLoading = false) }
                fetchSoonToExpireItems()
                fetchExpiryItems()
            } catch (e: Exception) {
                Log.e("ExpiryViewModel", "Failed to delete expiry item: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)) }
            }
        }
    }

    fun updateExpiryItem(itemId: Int, expiryDate: String?, quantity: Double?, unit: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                val request = UpdateExpiryRequest(
                    expiryDate = expiryDate,
                    quantity = quantity,
                    unit = unit
                )
                expiryRepository.updateExpiryItem(authHeader, itemId, request)
                _uiState.update { it.copy(isLoading = false) }
                fetchSoonToExpireItems()
                fetchExpiryItems()
            } catch (e: Exception) {
                Log.e("ExpiryViewModel", "Failed to update expiry item: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = com.teamconfused.planmyplate.util.NetworkUtils.parseError(e)) }
            }
        }
    }
}
