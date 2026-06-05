package com.teamconfused.planmyplate.data.repository

import android.util.Log
import com.teamconfused.planmyplate.data.mapper.toDomain
import com.teamconfused.planmyplate.data.model.UserPreferencesRequest
import com.teamconfused.planmyplate.domain.model.Diet
import com.teamconfused.planmyplate.domain.model.UserPreferences
import com.teamconfused.planmyplate.domain.repository.UserPreferencesRepository
import com.teamconfused.planmyplate.network.UserPreferencesService

class UserPreferencesRepositoryImpl(
    private val api: UserPreferencesService
) : UserPreferencesRepository {
    override suspend fun getPreferences(token: String, userId: Int): UserPreferences {
        return try {
            api.getPreferences(token, userId).toDomain()
        } catch (e: Exception) {
            Log.e("UserPreferencesRepo", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun setPreferences(token: String, userId: Int, request: UserPreferencesRequest): UserPreferences {
        return try {
            api.setPreferences(token, userId, request).toDomain()
        } catch (e: Exception) {
            Log.e("UserPreferencesRepo", "Operation failed: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getDiets(): List<Diet> {
        return try {
            api.getDiets().map { it.toDomain() }
        } catch (e: Exception) {
            Log.e("UserPreferencesRepo", "Operation failed: ${e.message}", e)
            throw e
        }
    }
}
