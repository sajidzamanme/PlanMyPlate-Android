package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.InventoryItemRequest
import com.teamconfused.planmyplate.domain.model.Inventory
import com.teamconfused.planmyplate.domain.model.InventoryItem
import com.teamconfused.planmyplate.domain.repository.InventoryRepository
import com.teamconfused.planmyplate.network.InventoryService

class InventoryRepositoryImpl(
    private val api: InventoryService
) : InventoryRepository {
    override suspend fun getInventoryForUser(token: String, userId: Int): Inventory {
        return try {
            api.getInventoryForUser(token, userId).toDomain()
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getInventoryById(token: String, id: Int): Inventory {
        return try {
            api.getInventoryById(token, id).toDomain()
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun createInventoryForUser(token: String, userId: Int): Inventory {
        return try {
            api.createInventoryForUser(token, userId).toDomain()
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateInventory(token: String, id: Int): Inventory {
        return try {
            api.updateInventory(token, id).toDomain()
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteInventory(token: String, id: Int) {
        try {
            api.deleteInventory(token, id)
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getInventoryItems(token: String, inventoryId: Int): List<InventoryItem> {
        return try {
            api.getInventoryItems(token, inventoryId).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun addItemToInventory(token: String, inventoryId: Int, request: InventoryItemRequest): InventoryItem {
        return try {
            api.addItemToInventory(token, inventoryId, request).toDomain()
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun removeItemFromInventory(token: String, itemId: Int) {
        try {
            api.removeItemFromInventory(token, itemId)
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateInventoryItem(token: String, itemId: Int, request: InventoryItemRequest): InventoryItem {
        return try {
            api.updateInventoryItem(token, itemId, request).toDomain()
        } catch (e: Exception) {
            Log.e("InventoryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }
}
