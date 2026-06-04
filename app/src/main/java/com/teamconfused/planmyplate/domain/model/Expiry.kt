package com.teamconfused.planmyplate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExpiryItem(
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
data class SoonToExpireResult(
    val thresholdDays: Int? = null,
    val totalCount: Int? = null,
    val expiredCount: Int? = null,
    val items: List<ExpiryItem>? = null
)
