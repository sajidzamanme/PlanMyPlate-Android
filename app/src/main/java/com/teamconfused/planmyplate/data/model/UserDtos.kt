package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val budget: Float? = null
)

@Serializable
data class UpdateUserRequest(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val phone: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val budget: Float? = null
)

@Serializable
data class UserPreferencesRequest(
    @SerialName("user_id") val userId: Int,
    val diet: String? = null,
    val allergies: List<String>? = null,
    val dislikes: List<String>? = null,
    val servings: Int? = null,
    val budget: Float? = null
)

@Serializable
data class UserPreferencesResponse(
    @SerialName("pref_id") val prefId: Int? = null,
    @SerialName("user_id") val userId: Int? = null,
    val diet: String? = null,
    val allergies: List<String>? = null,
    val dislikes: List<String>? = null,
    val servings: Int? = null,
    val budget: Float? = null,
    val age: Int? = null,
    val weight: Float? = null
)

@Serializable
data class DietDto(
    @SerialName("diet_id") val dietId: Int? = null,
    @SerialName("diet_name") val dietName: String? = null
)

@Serializable
data class AllergyDto(
    @SerialName("allergy_id") val allergyId: Int? = null,
    @SerialName("allergy_name") val allergyName: String? = null
)
