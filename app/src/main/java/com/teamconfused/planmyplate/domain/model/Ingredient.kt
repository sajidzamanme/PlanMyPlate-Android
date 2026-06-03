package com.teamconfused.planmyplate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val ingId: Int? = null,
    val name: String,
    val price: Float? = null
)