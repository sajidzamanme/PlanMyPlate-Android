package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    val tagId: Int,
    val tagName: String
)

@Serializable
data class IngredientDto(
    @SerialName("ingId") val ingId: Int? = null,
    val name: String,
    val price: Double? = null,
    val tags: List<TagDto>? = null
)

@Serializable
data class IngredientRequest(
    val name: String,
    val price: Float? = null
)

@Serializable
data class IngredientRefDto(
    @SerialName("ingId") val ingId: Int,
    val name: String? = null
)
