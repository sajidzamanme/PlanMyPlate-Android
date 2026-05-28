package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    @SerialName("date_of_birth") val dateOfBirth: String
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
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("user_id") val userId: Int? = null,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
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
    @SerialName("reset_token") val resetToken: String,
    @SerialName("new_password") val newPassword: String
)

@Serializable
data class ResetPasswordResponse(
    val message: String? = null
)
