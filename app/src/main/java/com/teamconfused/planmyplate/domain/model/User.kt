package com.teamconfused.planmyplate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: Int? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val age: Int? = null,
    val weight: Float? = null,
    val budget: Float? = null
)

@Serializable
data class UserPreferences(
    val prefId: Int? = null,
    val userId: Int? = null,
    val diet: String? = null,
    val allergies: List<String>? = null,
    val dislikes: List<String>? = null,
    val servings: Int? = null,
    val budget: Float? = null,
    val age: Int? = null,
    val weight: Float? = null
)

@Serializable
data class Diet(
    val dietId: Int? = null,
    val dietName: String? = null
)

@Serializable
data class Allergy(
    val allergyId: Int? = null,
    val allergyName: String? = null
)