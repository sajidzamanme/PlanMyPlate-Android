package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.CreateMealPlanRequest
import com.teamconfused.planmyplate.domain.model.MealPlan

interface MealPlanRepository {
    suspend fun getWeeklyMealPlans(token: String, userId: Int): List<MealPlan>
    suspend fun createMealPlanWithRecipes(token: String, userId: Int, request: CreateMealPlanRequest): MealPlan
}
