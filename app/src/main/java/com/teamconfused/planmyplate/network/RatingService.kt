package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.RecipeRatingRequest
import com.teamconfused.planmyplate.data.model.RecipeRatingResponse
import com.teamconfused.planmyplate.data.model.RecipeRatingSummary
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RatingService {
    @POST("api/ratings/")
    suspend fun rateRecipe(
        @Header("Authorization") token: String,
        @Body request: RecipeRatingRequest
    ): RecipeRatingResponse

    @GET("api/ratings/my/{recipeId}")
    suspend fun getMyRatingForRecipe(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: Int
    ): RecipeRatingResponse

    @GET("api/ratings/recipe/{recipeId}")
    suspend fun getRecipeRatingSummary(
        @Path("recipeId") recipeId: Int
    ): RecipeRatingSummary

    @DELETE("api/ratings/{recipeId}")
    suspend fun deleteMyRating(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: Int
    ): Map<String, String>
}
