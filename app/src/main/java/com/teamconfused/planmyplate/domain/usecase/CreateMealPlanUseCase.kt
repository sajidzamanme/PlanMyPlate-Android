package com.teamconfused.planmyplate.domain.usecase

import com.teamconfused.planmyplate.data.model.CreateMealPlanRequest
import com.teamconfused.planmyplate.domain.model.MealPlan
import com.teamconfused.planmyplate.domain.repository.MealPlanRepository

class CreateMealPlanUseCase(
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(token: String, userId: Int, request: CreateMealPlanRequest): MealPlan {
        return mealPlanRepository.createMealPlanWithRecipes(token, userId, request)
    }
}
