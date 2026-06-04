package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.InventoryItemRequest
import com.teamconfused.planmyplate.domain.model.Inventory
import com.teamconfused.planmyplate.domain.model.InventoryItem
import com.teamconfused.planmyplate.network.InventoryService
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
    private val inventoryService: InventoryService,
    private val sessionManager: SessionManager
) : ViewModel() {

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

                // Fetch user logic
                // API 6.1 Get Inventory for User
                val inventoryDto = inventoryService.getInventoryForUser(authHeader, userId)
                val inventory = inventoryDto.toDomain()
                
                // Then fetch items for that inventory
                val itemDtos = inventory.invId?.let { inventoryService.getInventoryItems(authHeader, it) } ?: emptyList()
                val items = itemDtos.map { it.toDomain() }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        inventory = inventory, 
                        items = items
                    ) 
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Failed to fetch inventory: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
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

             // Logic Switch:
             // 0 -> Delete (Immediate)
             // > 0 -> Debounced Update (or Add if that's the only working endpoint)
             
              if (newQty == 0.0 && item.itemId != null) {
                  try {
                      val token = sessionManager.getAuthToken() ?: return@launch
                      val authHeader = "Bearer $token"
                     inventoryService.removeItemFromInventory(authHeader, item.itemId)
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
                         // Use new UPDATE endpoint (requires backend impl)
                          val req = InventoryItemRequest(
                               quantity = newQty,
                               unit = item.unit,
                               expiryDate = item.expiryDate,
                               ingId = item.ingredient?.ingId
                           )
                         val token = sessionManager.getAuthToken() ?: return@launch
                         val authHeader = "Bearer $token"
                         inventoryService.updateInventoryItem(authHeader, item.itemId, req)
                     }
                  } catch (e: Exception) {
                      Log.e("InventoryViewModel", "Failed to update inventory item: ${e.message}", e)
                      // Fallback mechanism if Update fails? 
                      // Or just log.
                      println("Inventory sync failed: ${e.message}")
                  }
             }
        }
    }
}
