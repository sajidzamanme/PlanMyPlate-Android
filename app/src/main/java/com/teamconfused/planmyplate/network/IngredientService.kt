package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.IngredientDto
import com.teamconfused.planmyplate.data.model.IngredientRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IngredientService {
    @GET("api/ingredients/")
    suspend fun getAllIngredients(): List<IngredientDto>

    @GET("api/ingredients/{id}")
    suspend fun getIngredientById(@Path("id") id: Int): IngredientDto

    @POST("api/admin/ingredients")
    suspend fun createIngredient(
        @Header("Authorization") token: String,
        @Body request: IngredientRequest
    ): IngredientDto

    @PUT("api/admin/ingredients/{id}")
    suspend fun updateIngredient(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: IngredientRequest
    ): IngredientDto

    @DELETE("api/admin/ingredients/{id}")
    suspend fun deleteIngredient(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Map<String, String>

    @GET("api/ingredients/search")
    suspend fun searchIngredientsByName(@Query("name") name: String): List<IngredientDto>
}
