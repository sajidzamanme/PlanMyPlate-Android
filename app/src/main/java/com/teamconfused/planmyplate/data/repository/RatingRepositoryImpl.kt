package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.RecipeRatingRequest
import com.teamconfused.planmyplate.domain.model.RecipeRating
import com.teamconfused.planmyplate.domain.model.RecipeRatingSummary
import com.teamconfused.planmyplate.domain.repository.RatingRepository
import com.teamconfused.planmyplate.network.RatingService

class RatingRepositoryImpl(
    private val api: RatingService
) : RatingRepository {
    override suspend fun rateRecipe(token: String, request: RecipeRatingRequest): RecipeRating {
        return try {
            api.rateRecipe(token, request).toDomain()
        } catch (e: Exception) {
            Log.e("RatingRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getMyRatingForRecipe(token: String, recipeId: Int): RecipeRating {
        return try {
            api.getMyRatingForRecipe(token, recipeId).toDomain()
        } catch (e: Exception) {
            Log.e("RatingRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getRecipeRatingSummary(recipeId: Int): RecipeRatingSummary {
        return try {
            api.getRecipeRatingSummary(recipeId).toDomain()
        } catch (e: Exception) {
            Log.e("RatingRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteMyRating(token: String, recipeId: Int) {
        try {
            api.deleteMyRating(token, recipeId)
        } catch (e: Exception) {
            Log.e("RatingRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }
}
