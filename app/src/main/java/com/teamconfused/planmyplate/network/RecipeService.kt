package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.CreateRecipeRequest
import com.teamconfused.planmyplate.data.model.ImageUploadResponse
import com.teamconfused.planmyplate.data.model.RecipeResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeService {
    @GET("api/recipes/")
    suspend fun getAllRecipes(@Header("Authorization") token: String): List<RecipeResponse>

    @GET("api/recipes/{id}")
    suspend fun getRecipeById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): RecipeResponse

    @Multipart
    @POST("api/files/upload")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ImageUploadResponse

    @POST("api/recipes/")
    suspend fun createRecipe(
        @Header("Authorization") token: String,
        @Body request: CreateRecipeRequest
    ): RecipeResponse

    @PUT("api/recipes/{id}")
    suspend fun updateRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateRecipeRequest
    ): RecipeResponse

    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Map<String, String>

    @GET("api/recipes/search")
    suspend fun searchRecipesByName(
        @Header("Authorization") token: String,
        @Query("name") name: String
    ): List<RecipeResponse>

    @GET("api/recipes/filter/calories")
    suspend fun getRecipesByCalories(
        @Header("Authorization") token: String,
        @Query("minCalories") minCalories: Int,
        @Query("maxCalories") maxCalories: Int
    ): List<RecipeResponse>
}
