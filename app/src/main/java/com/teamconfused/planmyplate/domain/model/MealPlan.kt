package com.teamconfused.planmyplate.domain.model

import com.teamconfused.planmyplate.domain.model.Recipe
import kotlinx.serialization.Serializable

@Serializable
data class MealPlan(
    val mpId: Int? = null,
    val userId: Int? = null,
    val startDate: String? = null,
    val duration: Int,
    val status: String = "active",
    val slots: List<MealSlot>? = null,
    val createdAt: String? = null
)

@Serializable
data class MealSlot(
    val slotId: Int? = null,
    val mealType: String, // Breakfast, Lunch, Dinner
    val date: String? = null,
    val dayNumber: Int? = null,
    val recipe: Recipe? = null
)

@Serializable
data class AdditionalMeal(
    val recipeId: Int,
    val recipe: Recipe,
    val date: String,
    val mealType: String
)
