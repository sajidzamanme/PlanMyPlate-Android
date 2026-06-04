package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.GroceryListDto
import com.teamconfused.planmyplate.data.model.GroceryListItemDto
import com.teamconfused.planmyplate.data.model.PurchaseItemsRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GroceryListService {
    @GET("api/grocery-lists/user/{userId}")
    suspend fun getGroceryListsForUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): List<GroceryListDto>

    @GET("api/grocery-lists/{id}")
    suspend fun getGroceryListById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): GroceryListDto

    @POST("api/grocery-lists/user/{userId}")
    suspend fun createGroceryList(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: Map<String, String>
    ): GroceryListDto

    @PUT("api/grocery-lists/{id}")
    suspend fun updateGroceryList(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, String>
    ): GroceryListDto

    @POST("api/grocery-lists/{id}/purchase")
    suspend fun purchaseItems(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: PurchaseItemsRequest
    ): retrofit2.Response<Unit>

    @DELETE("api/grocery-lists/{id}")
    suspend fun deleteGroceryList(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Map<String, String>

    @GET("api/grocery-lists/user/{userId}/status/{status}")
    suspend fun getGroceryListsByStatus(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Path("status") status: String
    ): List<GroceryListDto>

    @PUT("api/grocery-lists/{listId}/items/{itemId}")
    suspend fun updateGroceryListItem(
        @Header("Authorization") token: String,
        @Path("listId") listId: Int,
        @Path("itemId") itemId: Int,
        @Body request: Map<String, Any>
    ): GroceryListItemDto
}
