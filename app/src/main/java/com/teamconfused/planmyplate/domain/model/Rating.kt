package com.teamconfused.planmyplate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeRating(
    val ratingId: Int? = null,
    val userId: Int? = null,
    val recipeId: Int? = null,
    val rating: Int,
    val review: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class RecipeRatingSummary(
    val recipeId: Int,
    val averageRating: Double,
    val totalRatings: Int
)
