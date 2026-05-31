package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.CreateMealPlanRequest
import com.teamconfused.planmyplate.data.model.MealPlanDto
import com.teamconfused.planmyplate.data.model.MealPlanUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MealPlanService {
    @GET("api/meal-plans/user/{userId}")
    suspend fun getAllMealPlansForUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): List<MealPlanDto>

    @GET("api/meal-plans/{id}")
    suspend fun getMealPlanById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): MealPlanDto

    @POST("api/meal-plans/user/{userId}/create")
    suspend fun createMealPlanWithRecipes(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: CreateMealPlanRequest
    ): MealPlanDto

    @PUT("api/meal-plans/{id}")
    suspend fun updateMealPlan(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: MealPlanUpdateRequest
    ): MealPlanDto

    @DELETE("api/meal-plans/{id}")
    suspend fun deleteMealPlan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Map<String, String>

    @GET("api/meal-plans/user/{userId}/status/{status}")
    suspend fun getMealPlansByStatus(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("status") status: String
    ): List<MealPlanDto>

    @GET("api/meal-plans/user/{userId}/weekly")
    suspend fun getWeeklyMealPlans(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): List<MealPlanDto>
}
