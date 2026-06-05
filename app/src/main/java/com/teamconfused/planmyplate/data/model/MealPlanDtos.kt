package com.teamconfused.planmyplate.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealPlanDto(
    @SerialName("mpId") val mpId: Int? = null,
    @SerialName("userId") val userId: Int? = null,
    @SerialName("startDate") val startDate: String? = null,
    val duration: Int,
    val status: String = "active",
    val slots: List<MealSlotDto>? = null
)

@Serializable
data class MealSlotDto(
    val id: Int? = null,
    @SerialName("slotIndex") val slotIndex: Int? = null,
    @SerialName("mealType") val mealType: String,
    @SerialName("dayNumber") val dayNumber: Int? = null,
    @SerialName("servingsMultiplier") val servingsMultiplier: Int? = null,
    val recipe: RecipeResponse? = null
)

@Serializable
data class CreateMealPlanRequest(
    @SerialName("recipeIds") val recipeIds: List<Int>,
    @SerialName("servingsMultipliers") val servingsMultipliers: List<Int>? = null,
    val duration: Int,
    @SerialName("startDate") val startDate: String
)

@Serializable
data class MealPlanUpdateRequest(
    val status: String? = null,
    val duration: Int? = null
)
