package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GroceryListDto(
    val listId: Int? = null,
    val userId: Int? = null,
    val dateCreated: String? = null,
    val status: String = "active",
    val items: List<GroceryListItemDto>? = null
)

@Serializable
data class PurchaseItemDetail(
    val itemId: Int,
    val quantity: Double
)

@Serializable
data class PurchaseItemsRequest(
    val items: List<PurchaseItemDetail>
)

@Serializable
data class GroceryListItemDto(
    val id: Int? = null,
    val ingredient: IngredientDto? = null,
    val quantity: Double? = null,
    val unit: String? = null
)
