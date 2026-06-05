package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.domain.model.UserFavorite

interface FavoriteRepository {
    suspend fun addFavorite(token: String, recipeId: Int): UserFavorite
    suspend fun removeFavorite(token: String, recipeId: Int)
    suspend fun getFavorites(token: String, skip: Int = 0, limit: Int = 100): List<UserFavorite>
    suspend fun isFavorite(token: String, recipeId: Int): Boolean
}
