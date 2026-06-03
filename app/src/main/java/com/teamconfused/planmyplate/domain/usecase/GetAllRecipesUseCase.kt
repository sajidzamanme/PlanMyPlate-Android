package com.teamconfused.planmyplate.domain.usecase

import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.repository.RecipeRepository

class GetAllRecipesUseCase(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(token: String): List<Recipe> {
        return repository.getAllRecipes(token)
    }
}
