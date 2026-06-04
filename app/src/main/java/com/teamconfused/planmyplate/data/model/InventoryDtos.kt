package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryDto(
    val invId: Int? = null,
    val userId: Int? = null,
    val lastUpdate: String? = null,
    val items: List<InventoryItemDto>? = null
)

@Serializable
data class InventoryItemDto(
    val itemId: Int? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val dateAdded: String? = null,
    val expiryDate: String? = null,
    val ingredient: IngredientDto? = null
)

@Serializable
data class InventoryItemRequest(
    val ingId: Int? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val expiryDate: String? = null
)
