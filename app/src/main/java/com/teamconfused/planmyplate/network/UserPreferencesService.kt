package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.AllergyDto
import com.teamconfused.planmyplate.data.model.DietDto
import com.teamconfused.planmyplate.data.model.IngredientDto
import com.teamconfused.planmyplate.data.model.UserPreferencesRequest
import com.teamconfused.planmyplate.data.model.UserPreferencesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UserPreferencesService {
    @POST("api/user-preferences/{userId}")
    suspend fun setPreferences(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body dto: UserPreferencesRequest
    ): UserPreferencesResponse

    @GET("api/user-preferences/{userId}")
    suspend fun getPreferences(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): UserPreferencesResponse

    @GET("api/reference-data/diets")
    suspend fun getDiets(): List<DietDto>
}
