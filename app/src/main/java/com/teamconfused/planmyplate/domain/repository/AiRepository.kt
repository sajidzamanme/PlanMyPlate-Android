package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.GenerateRecipeRequest
import com.teamconfused.planmyplate.domain.model.MealPlan
import com.teamconfused.planmyplate.domain.model.Recipe

interface AiRepository {
    suspend fun generateRecipe(token: String, request: GenerateRecipeRequest): Recipe
    suspend fun generateMealPlan(token: String, userId: Int, startDate: String? = null): MealPlan
}
