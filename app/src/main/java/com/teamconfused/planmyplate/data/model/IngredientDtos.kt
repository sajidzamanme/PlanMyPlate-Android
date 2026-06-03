package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IngredientDto(
    @SerialName("ing_id") val ingId: Int? = null,
    val name: String,
    val price: Double? = null,
    val tags: List<String>? = null
)

@Serializable
data class IngredientRequest(
    val name: String,
    val price: Float? = null
)

@Serializable
data class IngredientRefDto(
    @SerialName("ing_id") val ingId: Int,
    val name: String? = null
)
