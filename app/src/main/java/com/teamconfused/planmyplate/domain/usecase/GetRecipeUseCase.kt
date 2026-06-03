package com.teamconfused.planmyplate.domain.usecase

import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.repository.RecipeRepository

class GetRecipeUseCase(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(token: String, id: Int): Recipe {
        return repository.getRecipeById(token, id)
    }
}
