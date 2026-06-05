package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.PurchaseItemsRequest
import com.teamconfused.planmyplate.domain.model.GroceryList
import com.teamconfused.planmyplate.domain.model.GroceryListItem

interface GroceryRepository {
    suspend fun getGroceryListsForUser(token: String, userId: Int): List<GroceryList>
    suspend fun getGroceryListById(token: String, id: Int): GroceryList
    suspend fun getGroceryListsByStatus(token: String, userId: Int, status: String): List<GroceryList>
    suspend fun updateGroceryList(token: String, id: Int, status: String): GroceryList
    suspend fun deleteGroceryList(token: String, id: Int)
    suspend fun purchaseItems(token: String, id: Int, request: PurchaseItemsRequest)
    suspend fun updateGroceryListItem(token: String, listId: Int, itemId: Int, quantity: Double, unit: String?): GroceryListItem
}
