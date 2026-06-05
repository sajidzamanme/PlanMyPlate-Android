package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.domain.model.UserFavorite
import com.teamconfused.planmyplate.domain.repository.FavoriteRepository
import com.teamconfused.planmyplate.network.FavoriteService

class FavoriteRepositoryImpl(
    private val api: FavoriteService
) : FavoriteRepository {
    override suspend fun addFavorite(token: String, recipeId: Int): UserFavorite {
        return try {
            api.addFavorite(token, recipeId).toDomain()
        } catch (e: Exception) {
            Log.e("FavoriteRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun removeFavorite(token: String, recipeId: Int) {
        try {
            api.removeFavorite(token, recipeId)
        } catch (e: Exception) {
            Log.e("FavoriteRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getFavorites(token: String, skip: Int, limit: Int): List<UserFavorite> {
        return try {
            api.getFavorites(token, skip, limit).map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("FavoriteRepositoryImpl", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun isFavorite(token: String, recipeId: Int): Boolean {
        return try {
            api.checkFavoriteStatus(token, recipeId).isFavorite
        } catch (e: Exception) {
            Log.e("FavoriteRepositoryImpl", "Operation failed: ${e.message}", e)
            false
        }
    }
}
