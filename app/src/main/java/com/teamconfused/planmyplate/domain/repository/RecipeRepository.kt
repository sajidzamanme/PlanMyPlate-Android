package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.CreateRecipeRequest
import com.teamconfused.planmyplate.domain.model.Recipe
import okhttp3.MultipartBody

interface RecipeRepository {
    suspend fun getAllRecipes(token: String): List<Recipe>
    suspend fun getRecipeById(token: String, id: Int): Recipe
    suspend fun createRecipe(token: String, request: CreateRecipeRequest): Recipe
    suspend fun updateRecipe(token: String, id: Int, request: CreateRecipeRequest): Recipe
    suspend fun deleteRecipe(token: String, id: Int)
    suspend fun searchRecipes(token: String, query: String): List<Recipe>
    suspend fun getRecipesByCalories(token: String, min: Int, max: Int): List<Recipe>
    suspend fun uploadImage(token: String, file: MultipartBody.Part): String
    suspend fun cookRecipe(token: String, id: Int, servings: Float? = null, force: Boolean? = null): Map<String, String>
}
