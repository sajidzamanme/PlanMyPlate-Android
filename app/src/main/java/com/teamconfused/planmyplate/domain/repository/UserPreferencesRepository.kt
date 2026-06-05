package com.teamconfused.planmyplate.domain.repository

import com.teamconfused.planmyplate.data.model.UserPreferencesRequest
import com.teamconfused.planmyplate.domain.model.Diet
import com.teamconfused.planmyplate.domain.model.UserPreferences

interface UserPreferencesRepository {
    suspend fun getPreferences(token: String, userId: Int): UserPreferences
    suspend fun setPreferences(token: String, userId: Int, request: UserPreferencesRequest): UserPreferences
    suspend fun getDiets(): List<Diet>
}
