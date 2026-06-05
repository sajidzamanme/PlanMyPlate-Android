package com.teamconfused.planmyplate.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val recipeId: Int? = null,
    val name: String,
    val description: String,
    val calories: Int,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val prepTime: Int? = null,
    val cookTime: Int? = null,
    val servings: Int? = null,
    val instructions: String? = null,
    val ingredients: List<String>? = null,
    val imageUrl: String? = null
)
