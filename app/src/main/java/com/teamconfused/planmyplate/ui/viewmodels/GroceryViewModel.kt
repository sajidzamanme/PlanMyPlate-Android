package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.model.PurchaseItemDetail
import com.teamconfused.planmyplate.data.model.PurchaseItemsRequest
import com.teamconfused.planmyplate.domain.model.GroceryList
import com.teamconfused.planmyplate.domain.model.GroceryListItem
import com.teamconfused.planmyplate.domain.repository.GroceryRepository
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroceryUiState(
    val groceryLists: List<GroceryList> = emptyList(),
    val activeListItems: List<GroceryListItem> = emptyList(),
    val purchaseQuantities: Map<Int, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeListId: Int? = null
)

class GroceryViewModel(
    private val groceryRepository: GroceryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private var updateJob: kotlinx.coroutines.Job? = null
    private val _uiState = MutableStateFlow(GroceryUiState())
    val uiState: StateFlow<GroceryUiState> = _uiState.asStateFlow()

    init {
    }

    fun fetchGroceryLists() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"
                
                // 1. Fetch all lists
                val lists = groceryRepository.getGroceryListsForUser(authHeader, userId)
                
                // 3. Process Active Grocery List
                val activeList = lists.find { it.status == "active" } ?: lists.firstOrNull()
                val items = activeList?.items ?: emptyList()

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        groceryLists = lists,
                        activeListId = activeList?.listId,
                        activeListItems = items 
                    ) 
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Failed to fetch grocery lists: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    
    fun setPurchaseQuantity(itemId: Int, quantity: Double?) {
        _uiState.update { state ->
            val updatedMap = state.purchaseQuantities.toMutableMap()
            if (quantity == null || quantity <= 0.0) {
                updatedMap.remove(itemId)
            } else {
                updatedMap[itemId] = quantity
            }
            state.copy(purchaseQuantities = updatedMap)
        }
    }

    fun updateListQuantity(item: GroceryListItem, delta: Double) {
        val currentQty = item.quantity ?: 1.0
        val newQty = (currentQty + delta).coerceAtLeast(0.0)
        
        if (newQty == currentQty) return

        // 1. Optimistic Local Update
        _uiState.update { state ->
            val updatedItems = state.activeListItems.map { listItem ->
                if (listItem.id == item.id) listItem.copy(quantity = newQty) else listItem
            }
            state.copy(activeListItems = updatedItems)
        }

        // 2. Debounced API Call
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce 500ms
            try {
                _uiState.value.activeListId?.let { listId ->
                    val itemId = item.id ?: return@let
                    val token = sessionManager.getAuthToken() ?: return@launch
                    val authHeader = "Bearer $token"
                    groceryRepository.updateGroceryListItem(authHeader, listId, itemId, newQty, item.unit)
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Failed to update grocery list item quantity: ${e.message}", e)
                println("Failed to sync quantity: ${e.message}")
            }
        }
    }

    fun setListQuantity(item: GroceryListItem, newQty: Double) {
        val validatedQty = newQty.coerceAtLeast(0.0)
        val currentQty = item.quantity ?: 1.0
        if (validatedQty == currentQty) return

        // 1. Optimistic Local Update
        _uiState.update { state ->
            val updatedItems = state.activeListItems.map { listItem ->
                if (listItem.id == item.id) listItem.copy(quantity = validatedQty) else listItem
            }
            state.copy(activeListItems = updatedItems)
        }

        // 2. Debounced API Call
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce 500ms
            try {
                _uiState.value.activeListId?.let { listId ->
                    val itemId = item.id ?: return@let
                    val token = sessionManager.getAuthToken() ?: return@launch
                    val authHeader = "Bearer $token"
                    groceryRepository.updateGroceryListItem(authHeader, listId, itemId, validatedQty, item.unit)
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Failed to update grocery list item quantity: ${e.message}", e)
                println("Failed to sync quantity: ${e.message}")
            }
        }
    }

    fun purchaseSelectedItems(onSuccess: () -> Unit) {
        val listId = _uiState.value.activeListId ?: return
        val purchaseQuantities = _uiState.value.purchaseQuantities
        if (purchaseQuantities.isEmpty()) return
        
        // Map selected items to PurchaseItemDetail with custom quantities
        val purchaseItems = _uiState.value.activeListItems
            .mapNotNull { item ->
                val itemId = item.id ?: return@mapNotNull null
                val selectedQty = purchaseQuantities[itemId] ?: return@mapNotNull null
                if (selectedQty <= 0.0) return@mapNotNull null
                PurchaseItemDetail(
                    itemId = itemId,
                    quantity = selectedQty
                )
            }

        if (purchaseItems.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"

                val request = PurchaseItemsRequest(items = purchaseItems)
                groceryRepository.purchaseItems(authHeader, listId, request)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        purchaseQuantities = emptyMap()
                    ) 
                }
                fetchGroceryLists() // Refresh to see reduced list
                onSuccess()
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Failed to purchase selected items: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
