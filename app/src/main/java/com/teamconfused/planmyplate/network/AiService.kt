package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.MealPlanDto
import com.teamconfused.planmyplate.data.model.GenerateRecipeRequest
import com.teamconfused.planmyplate.data.model.RecipeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AiService {
    @POST("api/ai/generate-recipe")
    suspend fun generateRecipe(
        @Header("Authorization") token: String,
        @Body request: GenerateRecipeRequest
    ): RecipeResponse

    @POST("api/ai/generate-meal-plan")
    suspend fun generateMealPlan(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int,
        @Query("startDate") startDate: String? = null
    ): MealPlanDto
}
