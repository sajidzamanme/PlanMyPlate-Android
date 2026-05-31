package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealPlanDto(
    @SerialName("mp_id") val mpId: Int? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("start_date") val startDate: String? = null,
    val duration: Int,
    val status: String = "active",
    val slots: List<MealSlotDto>? = null
)

@Serializable
data class MealSlotDto(
    val id: Int? = null,
    @SerialName("slot_index") val slotIndex: Int? = null,
    @SerialName("meal_type") val mealType: String,
    @SerialName("day_number") val dayNumber: Int? = null,
    @SerialName("servings_multiplier") val servingsMultiplier: Int? = null,
    val recipe: RecipeResponse? = null
)

@Serializable
data class CreateMealPlanRequest(
    @SerialName("recipe_ids") val recipeIds: List<Int>,
    @SerialName("servings_multipliers") val servingsMultipliers: List<Int>? = null,
    val duration: Int,
    @SerialName("start_date") val startDate: String
)

@Serializable
data class MealPlanUpdateRequest(
    val status: String? = null,
    val duration: Int? = null
)
