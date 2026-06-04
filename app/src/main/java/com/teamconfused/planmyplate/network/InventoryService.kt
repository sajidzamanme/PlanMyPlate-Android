package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.InventoryDto
import com.teamconfused.planmyplate.data.model.InventoryItemDto
import com.teamconfused.planmyplate.data.model.InventoryItemRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface InventoryService {
    @GET("api/inventory/user/{userId}")
    suspend fun getInventoryForUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): InventoryDto

    @GET("api/inventory/{id}")
    suspend fun getInventoryById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): InventoryDto

    @POST("api/inventory/user/{userId}")
    suspend fun createInventoryForUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): InventoryDto

    @PUT("api/inventory/{id}")
    suspend fun updateInventory(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, Any> = emptyMap()
    ): InventoryDto

    @DELETE("api/inventory/{id}")
    suspend fun deleteInventory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Map<String, String>

    @GET("api/inventory/{inventoryId}/items")
    suspend fun getInventoryItems(
        @Header("Authorization") token: String,
        @Path("inventoryId") inventoryId: Int
    ): List<InventoryItemDto>

    @POST("api/inventory/{inventoryId}/items")
    suspend fun addItemToInventory(
        @Header("Authorization") token: String,
        @Path("inventoryId") inventoryId: Int,
        @Body request: InventoryItemRequest
    ): InventoryItemDto

    @DELETE("api/inventory/items/{itemId}")
    suspend fun removeItemFromInventory(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Int
    ): Map<String, String>

    @PUT("api/inventory/items/{itemId}")
    suspend fun updateInventoryItem(
        @Header("Authorization") token: String,
        @Path("itemId") itemId: Int, 
        @Body request: InventoryItemRequest
    ): InventoryItemDto
}
