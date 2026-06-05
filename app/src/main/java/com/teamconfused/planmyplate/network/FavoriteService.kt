package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.UserFavoriteResponse
import com.teamconfused.planmyplate.data.model.FavoriteStatusResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoriteService {
    @POST("api/favorites/{recipeId}")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: Int
    ): UserFavoriteResponse

    @DELETE("api/favorites/{recipeId}")
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: Int
    ): Map<String, String>

    @GET("api/favorites/")
    suspend fun getFavorites(
        @Header("Authorization") token: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): List<UserFavoriteResponse>

    @GET("api/favorites/{recipeId}/status")
    suspend fun checkFavoriteStatus(
        @Header("Authorization") token: String,
        @Path("recipeId") recipeId: Int
    ): FavoriteStatusResponse
}
