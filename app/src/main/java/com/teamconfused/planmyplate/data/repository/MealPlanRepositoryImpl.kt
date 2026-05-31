package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.CreateMealPlanRequest
import com.teamconfused.planmyplate.domain.model.MealPlan
import com.teamconfused.planmyplate.domain.repository.MealPlanRepository
import com.teamconfused.planmyplate.network.MealPlanService

class MealPlanRepositoryImpl(
    private val api: MealPlanService
) : MealPlanRepository {
    override suspend fun getWeeklyMealPlans(token: String, userId: Int): List<MealPlan> {
        return try {
            api.getWeeklyMealPlans(token, userId).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("MealPlanRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun createMealPlanWithRecipes(
        token: String,
        userId: Int,
        request: CreateMealPlanRequest
    ): MealPlan {
        return try {
            api.createMealPlanWithRecipes(token, userId, request).toDomain()
        } catch (e: Exception) {
            Log.e("MealPlanRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }
}
