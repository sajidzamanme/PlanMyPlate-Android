package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.InventoryItemRequest
import com.teamconfused.planmyplate.domain.model.Inventory
import com.teamconfused.planmyplate.domain.model.InventoryItem

interface InventoryRepository {
    suspend fun getInventoryForUser(token: String, userId: Int): Inventory
    suspend fun getInventoryById(token: String, id: Int): Inventory
    suspend fun createInventoryForUser(token: String, userId: Int): Inventory
    suspend fun updateInventory(token: String, id: Int): Inventory
    suspend fun deleteInventory(token: String, id: Int)
    suspend fun getInventoryItems(token: String, inventoryId: Int): List<InventoryItem>
    suspend fun addItemToInventory(token: String, inventoryId: Int, request: InventoryItemRequest): InventoryItem
    suspend fun removeItemFromInventory(token: String, itemId: Int)
    suspend fun updateInventoryItem(token: String, itemId: Int, request: InventoryItemRequest): InventoryItem
}
