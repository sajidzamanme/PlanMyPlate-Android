package com.teamconfused.planmyplate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserFavorite(
    val id: Int? = null,
    val userId: Int? = null,
    val recipeId: Int? = null,
    val recipe: Recipe? = null,
    val createdAt: String? = null
)
