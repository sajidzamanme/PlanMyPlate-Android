package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeRatingRequest(
    val recipeId: Int,
    val rating: Int,
    val review: String? = null
)

@Serializable
data class RecipeRatingResponse(
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
