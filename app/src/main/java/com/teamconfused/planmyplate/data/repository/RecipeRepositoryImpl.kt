package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.CreateRecipeRequest
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.repository.RecipeRepository
import com.teamconfused.planmyplate.network.RecipeService
import okhttp3.MultipartBody

class RecipeRepositoryImpl(
    private val api: RecipeService
) : RecipeRepository {
    override suspend fun getAllRecipes(token: String): List<Recipe> {
        return try {
            api.getAllRecipes(token).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getRecipeById(token: String, id: Int): Recipe {
        return try {
            api.getRecipeById(token, id).toDomain()
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun createRecipe(token: String, request: CreateRecipeRequest): Recipe {
        return try {
            api.createRecipe(token, request).toDomain()
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateRecipe(token: String, id: Int, request: CreateRecipeRequest): Recipe {
        return try {
            api.updateRecipe(token, id, request).toDomain()
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteRecipe(token: String, id: Int) {
        try {
            api.deleteRecipe(token, id)
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun searchRecipes(token: String, query: String): List<Recipe> {
        return try {
            api.searchRecipesByName(token, query).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getRecipesByCalories(token: String, min: Int, max: Int): List<Recipe> {
        return try {
            api.getRecipesByCalories(token, min, max).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun uploadImage(token: String, file: MultipartBody.Part): String {
        return try {
            api.uploadImage(token, file).url
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun cookRecipe(token: String, id: Int, servings: Float?, force: Boolean?): Map<String, String> {
        return try {
            api.cookRecipe(token, id, servings, force)
        } catch (e: Exception) {
            Log.e("RecipeRepositoryImpl", "cookRecipe failed: ${e.message}", e)
            throw e
        }
    }
}
