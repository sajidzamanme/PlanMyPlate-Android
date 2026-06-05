package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(
    @SerialName("recipeId") val recipeId: Int? = null,
    val name: String,
    val description: String? = null,
    val calories: Int? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    @SerialName("prepTime") val prepTime: Int? = null,
    @SerialName("cookTime") val cookTime: Int? = null,
    val servings: Int? = null,
    val instructions: String? = null,
    @SerialName("recipeIngredients") val recipeIngredients: List<RecipeIngredientResponse>? = null,
    @SerialName("imageUrl") val imageUrl: String? = null
)

@Serializable
data class RecipeIngredientResponse(
    val id: Int? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val ingredient: IngredientDto? = null
)

@Serializable
data class CreateRecipeRequest(
    val name: String,
    val description: String? = null,
    val calories: Int? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    @SerialName("prepTime") val prepTime: Int? = null,
    @SerialName("cookTime") val cookTime: Int? = null,
    val servings: Int? = null,
    val instructions: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    val ingredients: List<RecipeIngredientRequest>? = null
)

@Serializable
data class RecipeIngredientRequest(
    @SerialName("ingId") val ingId: Int,
    val quantity: Int,
    val unit: String
)

@Serializable
data class ImageUploadResponse(
    val url: String,
    val filename: String
)

@Serializable
data class GenerateRecipeRequest(
    @SerialName("availableIngredients") val availableIngredients: List<String> = emptyList(),
    @SerialName("maxCalories") val maxCalories: Int? = null,
    @SerialName("cuisineType") val cuisineType: String? = null,
    val allergies: List<String> = emptyList(),
    @SerialName("dietaryPreference") val dietaryPreference: String? = null,
    val mood: String? = null,
    val servings: Int = 2,
    @SerialName("maxCookingTime") val maxCookingTime: Int? = null
)

@Serializable
data class AdditionalMeal(
    @SerialName("recipeId") val recipeId: Int,
    val recipe: RecipeResponse,
    val date: String,
    @SerialName("mealType") val mealType: String
)

@Serializable
data class CookRecipeErrorResponse(
    val status: String,
    val title: String,
    val message: String,
    val missing: List<MissingIngredientDto> = emptyList()
)

@Serializable
data class MissingIngredientDto(
    @SerialName("ingId") val ingId: Int,
    val name: String,
    val required: Double,
    val available: Double,
    val unit: String
)
