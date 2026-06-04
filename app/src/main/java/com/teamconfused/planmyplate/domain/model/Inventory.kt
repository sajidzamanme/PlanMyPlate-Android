package com.teamconfused.planmyplate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Inventory(
    val invId: Int? = null,
    val userId: Int? = null,
    val lastUpdate: String? = null,
    val items: List<InventoryItem>? = null
)

@Serializable
data class InventoryItem(
    val itemId: Int? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val dateAdded: String? = null,
    val expiryDate: String? = null,
    val ingredient: Ingredient? = null
)
