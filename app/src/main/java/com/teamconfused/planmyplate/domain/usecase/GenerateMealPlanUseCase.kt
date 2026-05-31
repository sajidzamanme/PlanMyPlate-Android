package com.teamconfused.planmyplate.domain.usecase

import com.teamconfused.planmyplate.domain.model.MealPlan
import com.teamconfused.planmyplate.domain.repository.AiRepository

class GenerateMealPlanUseCase(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(token: String, userId: Int, startDate: String? = null): MealPlan {
        return aiRepository.generateMealPlan(token, userId, startDate)
    }
}
