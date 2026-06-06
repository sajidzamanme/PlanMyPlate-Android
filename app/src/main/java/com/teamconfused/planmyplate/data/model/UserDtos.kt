package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("userId") val userId: Int? = null,
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val budget: Float? = null
)

@Serializable
data class UpdateUserRequest(
    @SerialName("firstName") val firstName: String? = null,
    @SerialName("lastName") val lastName: String? = null,
    val phone: String? = null,
    @SerialName("dateOfBirth") val dateOfBirth: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val budget: Float? = null
)

@Serializable
data class UserPreferencesRequest(
    @SerialName("userId") val userId: Int,
    val diets: List<String>? = null,
    val allergies: List<String>? = null,
    val dislikes: List<String>? = null,
    val budget: Float? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val gender: String? = null
)

@Serializable
data class UserPreferencesResponse(
    @SerialName("prefId") val prefId: Int? = null,
    @SerialName("userId") val userId: Int? = null,
    val diets: List<String>? = null,
    val allergies: List<String>? = null,
    val dislikes: List<String>? = null,
    val budget: Float? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val gender: String? = null,
    val bmi: Double? = null,
    @SerialName("bmi_category") val bmiCategory: String? = null
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
