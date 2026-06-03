package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.IngredientDto
import com.teamconfused.planmyplate.data.model.IngredientRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IngredientService {
    @GET("api/ingredients/")
    suspend fun getAllIngredients(): List<IngredientDto>

    @GET("api/ingredients/{id}")
    suspend fun getIngredientById(@Path("id") id: Int): IngredientDto

    @POST("api/ingredients/")
    suspend fun createIngredient(@Body request: IngredientRequest): IngredientDto

    @PUT("api/ingredients/{id}")
    suspend fun updateIngredient(
        @Path("id") id: Int,
        @Body request: IngredientRequest
    ): IngredientDto

    @DELETE("api/ingredients/{id}")
    suspend fun deleteIngredient(@Path("id") id: Int): Map<String, String>

    @GET("api/ingredients/search")
    suspend fun searchIngredientsByName(@Query("name") name: String): List<IngredientDto>

    @GET("api/ingredients/price/range")
    suspend fun filterIngredientsByPrice(
        @Query("minPrice") minPrice: Float,
        @Query("maxPrice") maxPrice: Float
    ): List<IngredientDto>
}
