package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.ExpiryItemRequest
import com.teamconfused.planmyplate.data.model.UpdateExpiryRequest
import com.teamconfused.planmyplate.domain.model.ExpiryItem
import com.teamconfused.planmyplate.domain.model.SoonToExpireResult
import com.teamconfused.planmyplate.domain.repository.ExpiryRepository
import com.teamconfused.planmyplate.network.ExpiryService

class ExpiryRepositoryImpl(
    private val api: ExpiryService
) : ExpiryRepository {
    override suspend fun addExpiryItem(token: String, userId: Int, request: ExpiryItemRequest): ExpiryItem {
        return try {
            api.addExpiryItem(token, userId, request).toDomain()
        } catch (e: Exception) {
            Log.e("ExpiryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun listExpiryItems(token: String, userId: Int): List<ExpiryItem> {
        return try {
            api.listExpiryItems(token, userId).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("ExpiryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getSoonToExpireItems(token: String, userId: Int, days: Int): SoonToExpireResult {
        return try {
            api.getSoonToExpireItems(token, userId, days).toDomain()
        } catch (e: Exception) {
            Log.e("ExpiryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateExpiryItem(token: String, itemId: Int, request: UpdateExpiryRequest): ExpiryItem {
        return try {
            api.updateExpiryItem(token, itemId, request).toDomain()
        } catch (e: Exception) {
            Log.e("ExpiryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteExpiryItem(token: String, itemId: Int) {
        try {
            api.deleteExpiryItem(token, itemId)
        } catch (e: Exception) {
            Log.e("ExpiryRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }
}
