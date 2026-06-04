package com.teamconfused.planmyplate.domain.model

import com.teamconfused.planmyplate.domain.model.Ingredient
import kotlinx.serialization.Serializable

@Serializable
data class GroceryList(
    val listId: Int? = null,
    val userId: Int? = null,
    val dateCreated: String? = null,
    val status: String = "active",
    val items: List<GroceryListItem>? = null,
    val createdAt: String? = null
)

@Serializable
data class GroceryListItem(
    val id: Int? = null,
    val ingredient: Ingredient? = null,
    val quantity: Double? = null,
    val unit: String? = null
)
