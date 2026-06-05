package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.RecipeRatingRequest
import com.teamconfused.planmyplate.domain.model.RecipeRating
import com.teamconfused.planmyplate.domain.model.RecipeRatingSummary

interface RatingRepository {
    suspend fun rateRecipe(token: String, request: RecipeRatingRequest): RecipeRating
    suspend fun getMyRatingForRecipe(token: String, recipeId: Int): RecipeRating
    suspend fun getRecipeRatingSummary(recipeId: Int): RecipeRatingSummary
    suspend fun deleteMyRating(token: String, recipeId: Int)
}
