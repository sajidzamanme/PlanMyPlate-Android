package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.InventoryItemRequest
import com.teamconfused.planmyplate.domain.model.Inventory
import com.teamconfused.planmyplate.domain.model.InventoryItem
import com.teamconfused.planmyplate.domain.repository.InventoryRepository
import com.teamconfused.planmyplate.util.NetworkUtils
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventoryUiState(
    val inventory: Inventory? = null,
    val items: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val sessionManager: SessionManager,
    private val ingredientService: com.teamconfused.planmyplate.network.IngredientService
) : ViewModel() {

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
                Log.e("InventoryViewModel", "Search failed: ${e.message}", e)
            }
        }
    }

    private var updateJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
    }

    fun fetchInventory() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"

                // Fetch inventory for user
                val inventory = inventoryRepository.getInventoryForUser(authHeader, userId)
                val items = inventory.items ?: emptyList()
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        inventory = inventory, 
                        items = items
                    ) 
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Failed to fetch inventory: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = NetworkUtils.parseError(e)) }
            }
        }
    }
    
    fun updateItemQuantity(item: InventoryItem, delta: Int) {
        val inventoryId = _uiState.value.inventory?.invId ?: return
        val currentQty = item.quantity ?: 0.0
        val newQty = currentQty + delta
        
        if (newQty < 0) return 

        // Optimistic Local Update
        viewModelScope.launch {
              val updatedItems = _uiState.value.items.map {
                  if (it.itemId == item.itemId) it.copy(quantity = newQty) else it
              }.filter { (it.quantity ?: 0.0) > 0 } // Remove locally if 0
              
             _uiState.update { it.copy(items = updatedItems) }
             
              if (newQty == 0.0 && item.itemId != null) {
                  try {
                      val token = sessionManager.getAuthToken() ?: return@launch
                      val authHeader = "Bearer $token"
                      inventoryRepository.removeItemFromInventory(authHeader, item.itemId)
                  } catch (e: Exception) {
                      Log.e("InventoryViewModel", "Failed to remove item from inventory: ${e.message}", e)
                      fetchInventory() // Revert on failure
                  }
                  return@launch
              }

              // Debounced Update
              updateJob?.cancel()
              updateJob = launch {
                  kotlinx.coroutines.delay(500)
                  try {
                       if (delta != 0 && item.itemId != null) {
                           val req = InventoryItemRequest(
                                quantity = newQty,
                                unit = item.unit,
                                expiryDate = item.expiryDate,
                                ingId = item.ingredient?.ingId
                            )
                          val token = sessionManager.getAuthToken() ?: return@launch
                          val authHeader = "Bearer $token"
                          inventoryRepository.updateInventoryItem(authHeader, item.itemId, req)
                      }
                  } catch (e: Exception) {
                      Log.e("InventoryViewModel", "Failed to update inventory item: ${e.message}", e)
                      println("Inventory sync failed: ${e.message}")
                  }
              }
        }
    }

    fun updateInventoryItem(item: InventoryItem, newQty: Double, newExpiryDate: String?, newUnit: String?) {
        val inventoryId = _uiState.value.inventory?.invId ?: return
        val currentQty = item.quantity ?: 0.0
        
        if (newQty < 0.0) return 

        // Optimistic Local Update
        viewModelScope.launch {
              val updatedItems = _uiState.value.items.map {
                  if (it.itemId == item.itemId) {
                      it.copy(quantity = newQty, expiryDate = newExpiryDate, unit = newUnit)
                  } else {
                      it
                  }
              }.filter { (it.quantity ?: 0.0) > 0.0 } // Remove locally if 0
              
             _uiState.update { it.copy(items = updatedItems) }
             
              if (newQty == 0.0 && item.itemId != null) {
                  try {
                      val token = sessionManager.getAuthToken() ?: return@launch
                      val authHeader = "Bearer $token"
                      inventoryRepository.removeItemFromInventory(authHeader, item.itemId)
                  } catch (e: Exception) {
                      Log.e("InventoryViewModel", "Failed to remove item from inventory: ${e.message}", e)
                      fetchInventory() // Revert on failure
                  }
                  return@launch
              }

              // Update API
              try {
                   if (item.itemId != null) {
                       val req = InventoryItemRequest(
                            quantity = newQty,
                            unit = newUnit,
                            expiryDate = newExpiryDate,
                            ingId = item.ingredient?.ingId
                        )
                       val token = sessionManager.getAuthToken() ?: return@launch
                       val authHeader = "Bearer $token"
                       inventoryRepository.updateInventoryItem(authHeader, item.itemId, req)
                       fetchInventory() // Refresh to sync items
                   }
              } catch (e: Exception) {
                   Log.e("InventoryViewModel", "Failed to update inventory item: ${e.message}", e)
                   println("Inventory sync failed: ${e.message}")
                   fetchInventory() // Revert on failure
              }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
