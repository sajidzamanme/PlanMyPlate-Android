package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserFavoriteResponse(
    val id: Int? = null,
    val userId: Int? = null,
    val recipeId: Int? = null,
    val recipe: RecipeResponse? = null,
    val createdAt: String? = null
)

@Serializable
data class FavoriteStatusResponse(
    val isFavorite: Boolean
)
