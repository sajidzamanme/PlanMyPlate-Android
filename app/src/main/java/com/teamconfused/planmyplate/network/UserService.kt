package com.teamconfused.planmyplate.network

import com.teamconfused.planmyplate.data.model.UpdateUserRequest
import com.teamconfused.planmyplate.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {
    @GET("api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): UserDto

    @PUT("api/users/{userId}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Body request: UpdateUserRequest
    ): UserDto

    @DELETE("api/users/{userId}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Map<String, String>
}
