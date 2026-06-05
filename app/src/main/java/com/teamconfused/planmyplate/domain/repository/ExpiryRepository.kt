package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.ExpiryItemRequest
import com.teamconfused.planmyplate.data.model.UpdateExpiryRequest
import com.teamconfused.planmyplate.domain.model.ExpiryItem
import com.teamconfused.planmyplate.domain.model.SoonToExpireResult

interface ExpiryRepository {
    suspend fun addExpiryItem(token: String, userId: Int, request: ExpiryItemRequest): ExpiryItem
    suspend fun listExpiryItems(token: String, userId: Int): List<ExpiryItem>
    suspend fun getSoonToExpireItems(token: String, userId: Int, days: Int): SoonToExpireResult
    suspend fun updateExpiryItem(token: String, itemId: Int, request: UpdateExpiryRequest): ExpiryItem
    suspend fun deleteExpiryItem(token: String, itemId: Int)
}
