package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.ExpiryItemRequest
import com.teamconfused.planmyplate.data.model.ExpiryItemResponse
import com.teamconfused.planmyplate.data.model.SoonToExpireResponse
import com.teamconfused.planmyplate.data.model.UpdateExpiryRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpiryService {
    @POST("api/expiry/user/{userId}/items")
    suspend fun addExpiryItem(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: ExpiryItemRequest
    ): ExpiryItemResponse

    @GET("api/expiry/user/{userId}/items")
    suspend fun listExpiryItems(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): List<ExpiryItemResponse>

    @GET("api/expiry/user/{userId}/soon")
    suspend fun getSoonToExpireItems(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Query("days") days: Int = 10
    ): SoonToExpireResponse

    @PUT("api/expiry/items/{itemId}")
    suspend fun updateExpiryItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Int,
        @Body request: UpdateExpiryRequest
    ): ExpiryItemResponse

    @DELETE("api/expiry/items/{itemId}")
    suspend fun deleteExpiryItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Int
    ): Map<String, String>
}
