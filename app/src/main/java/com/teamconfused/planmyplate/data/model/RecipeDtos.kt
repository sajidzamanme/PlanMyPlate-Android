package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(
    @SerialName("recipe_id") val recipeId: Int? = null,
    val name: String,
    val description: String? = null,
    val calories: Int? = null,
    @SerialName("prep_time") val prepTime: Int? = null,
    @SerialName("cook_time") val cookTime: Int? = null,
    val servings: Int? = null,
    val instructions: String? = null,
    @SerialName("recipe_ingredients") val recipeIngredients: List<RecipeIngredientResponse>? = null,
    @SerialName("image_url") val imageUrl: String? = null
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
    @SerialName("prep_time") val prepTime: Int? = null,
    @SerialName("cook_time") val cookTime: Int? = null,
    val servings: Int? = null,
    val instructions: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val ingredients: List<RecipeIngredientRequest>? = null
)

@Serializable
data class RecipeIngredientRequest(
    @SerialName("ing_id") val ingId: Int,
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
    @SerialName("available_ingredients") val availableIngredients: List<String> = emptyList(),
    @SerialName("max_calories") val maxCalories: Int? = null,
    @SerialName("cuisine_type") val cuisineType: String? = null,
    val allergies: List<String> = emptyList(),
    @SerialName("dietary_preference") val dietaryPreference: String? = null,
    val mood: String? = null,
    val servings: Int = 2,
    @SerialName("max_cooking_time") val maxCookingTime: Int? = null
)

@Serializable
data class AdditionalMeal(
    @SerialName("recipe_id") val recipeId: Int,
    val recipe: RecipeResponse,
    val date: String,
    @SerialName("meal_type") val mealType: String
)
