package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.PurchaseItemsRequest
import com.teamconfused.planmyplate.domain.model.GroceryList
import com.teamconfused.planmyplate.domain.model.GroceryListItem
import com.teamconfused.planmyplate.domain.repository.GroceryRepository
import com.teamconfused.planmyplate.network.GroceryListService

class GroceryRepositoryImpl(
    private val api: GroceryListService
) : GroceryRepository {
    override suspend fun getGroceryListsForUser(token: String, userId: Int): List<GroceryList> {
        return try {
            api.getGroceryListsForUser(token, userId).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("GroceryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getGroceryListById(token: String, id: Int): GroceryList {
        return try {
            api.getGroceryListById(token, id).toDomain()
        } catch (e: Exception) {
            Log.e("GroceryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getGroceryListsByStatus(token: String, userId: Int, status: String): List<GroceryList> {
        return try {
            api.getGroceryListsByStatus(token, userId, status).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("GroceryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateGroceryList(token: String, id: Int, status: String): GroceryList {
        return try {
            val request = mapOf("status" to status)
            api.updateGroceryList(token, id, request).toDomain()
        } catch (e: Exception) {
            Log.e("GroceryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteGroceryList(token: String, id: Int) {
        try {
            api.deleteGroceryList(token, id)
        } catch (e: Exception) {
            Log.e("GroceryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun purchaseItems(token: String, id: Int, request: PurchaseItemsRequest) {
        try {
            api.purchaseItems(token, id, request)
        } catch (e: Exception) {
            Log.e("GroceryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateGroceryListItem(
        token: String,
        listId: Int,
        itemId: Int,
        quantity: Double,
        unit: String?
    ): GroceryListItem {
        return try {
            val reqMap = mutableMapOf<String, Any>("quantity" to quantity)
            if (unit != null) {
                reqMap["unit"] = unit
            }
            api.updateGroceryListItem(token, listId, itemId, reqMap).toDomain()
        } catch (e: Exception) {
            Log.e("GroceryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }
}
