package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    @SerialName("dateOfBirth") val dateOfBirth: String
)

@Serializable
data class SigninRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    val email: String? = null,
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    @SerialName("userId") val userId: Int? = null,
    val phone: String? = null,
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    val message: String? = null
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ForgotPasswordResponse(
    val message: String? = null
)

@Serializable
data class ResetPasswordRequest(
    @SerialName("resetToken") val resetToken: String,
    @SerialName("newPassword") val newPassword: String
)

@Serializable
data class ResetPasswordResponse(
    val message: String? = null
)
