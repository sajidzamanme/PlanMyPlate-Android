package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExpiryItemRequest(
    val productName: String,
    val expiryDate: String,
    val quantity: Double? = null,
    val unit: String? = null
)

@Serializable
data class ExpiryItemResponse(
    val itemId: Int? = null,
    val productName: String? = null,
    val expiryDate: String? = null,
    val dateAdded: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val daysUntilExpiry: Int? = null,
    val isExpired: Boolean? = null
)

@Serializable
data class SoonToExpireResponse(
    val thresholdDays: Int? = null,
    val totalCount: Int? = null,
    val expiredCount: Int? = null,
    val items: List<ExpiryItemResponse>? = null
)

@Serializable
data class UpdateExpiryRequest(
    val expiryDate: String? = null,
    val quantity: Double? = null,
    val unit: String? = null
)
