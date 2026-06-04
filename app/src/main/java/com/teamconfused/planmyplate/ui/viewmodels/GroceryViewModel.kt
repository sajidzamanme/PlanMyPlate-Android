package com.teamconfused.planmyplate.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.PurchaseItemDetail
import com.teamconfused.planmyplate.data.model.PurchaseItemsRequest
import com.teamconfused.planmyplate.domain.model.GroceryList
import com.teamconfused.planmyplate.domain.model.GroceryListItem
import com.teamconfused.planmyplate.network.GroceryListService
import com.teamconfused.planmyplate.network.InventoryService
import com.teamconfused.planmyplate.network.MealPlanService
import com.teamconfused.planmyplate.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroceryUiState(
    val groceryLists: List<GroceryList> = emptyList(),
    val activeListItems: List<GroceryListItem> = emptyList(),
    val checkedItems: Set<Int> = emptySet(), // IDs of items checked for purchase
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeListId: Int? = null
)

class GroceryViewModel(
    private val groceryListService: GroceryListService,
    private val mealPlanService: MealPlanService,
    private val inventoryService: InventoryService,
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
                val listDtos = groceryListService.getGroceryListsForUser(authHeader, userId)
                val lists = listDtos.map { it.toDomain() }
                
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

    
    // Toggle check status of an item
    fun toggleItemCheck(itemId: Int) {
        _uiState.update { 
            val current = it.checkedItems
            if (current.contains(itemId)) {
                it.copy(checkedItems = current - itemId)
            } else {
                it.copy(checkedItems = current + itemId)
            }
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
                    // Note: sending { "quantity": newQty }
                    val token = sessionManager.getAuthToken() ?: return@launch
                    val authHeader = "Bearer $token"
                    val req = mapOf("quantity" to newQty)
                    groceryListService.updateGroceryListItem(authHeader, listId, itemId, req)
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Failed to update grocery list item quantity: ${e.message}", e)
                // Ignore 404 if endpoint doesn't exist yet, or handle error
                println("Failed to sync quantity: ${e.message}")
            }
        }
    }

    fun purchaseSelectedItems(onSuccess: () -> Unit) {
        val listId = _uiState.value.activeListId ?: return
        val checkedIds = _uiState.value.checkedItems
        if (checkedIds.isEmpty()) return
        
        // Map checked items to PurchaseItemDetail with current UI quantities
        val purchaseItems = _uiState.value.activeListItems
            .filter { checkedIds.contains(it.id) }
            .mapNotNull { item ->
                val itemId = item.id ?: return@mapNotNull null
                PurchaseItemDetail(
                    itemId = itemId,
                    quantity = item.quantity ?: 1.0
                )
            }

        if (purchaseItems.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val token = sessionManager.getAuthToken() ?: return@launch
                val authHeader = "Bearer $token"

                val request = PurchaseItemsRequest(items = purchaseItems)
                val response = groceryListService.purchaseItems(authHeader, listId, request)
                
                if (response.isSuccessful) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            checkedItems = emptySet()
                        ) 
                    }
                    fetchGroceryLists() // Refresh to see reduced list
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Purchase failed") }
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Failed to purchase selected items: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
