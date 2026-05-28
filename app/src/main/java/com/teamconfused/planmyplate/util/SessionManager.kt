package com.teamconfused.planmyplate.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.teamconfused.planmyplate.domain.model.AdditionalMeal
import com.teamconfused.planmyplate.domain.model.UserPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) {
        prefs.edit(commit = true) { putInt("user_id", userId) }
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1
    }
    
    fun setHasMealPlans(hasMealPlans: Boolean) {
        prefs.edit { putBoolean("has_meal_plans", hasMealPlans) }
    }
    
    fun hasMealPlans(): Boolean {
        return prefs.getBoolean("has_meal_plans", false)
    }

    fun clearSession() {
        prefs.edit { clear() }
    }

    fun saveAuthToken(token: String) {
        prefs.edit(commit = true) { putString("auth_token", token) }
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUserPreferences(preferences: UserPreferences) {
        val json = Json.encodeToString(UserPreferences.serializer(), preferences)
        prefs.edit { putString("user_preferences", json) }
    }

    fun getUserPreferences(): UserPreferences {
        val json = prefs.getString("user_preferences", null)
        return if (json != null) {
            try {
                Json.decodeFromString(UserPreferences.serializer(), json)
            } catch (_: Exception) {
                UserPreferences()
            }
        } else {
            UserPreferences()
        }
    }

    // --- Local Sandbox Storage ---

    fun saveAdditionalMeals(meals: List<AdditionalMeal>) {
        val json = Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(AdditionalMeal.serializer()), meals)
        prefs.edit { putString("additional_meals", json) }
    }

    fun getAdditionalMeals(): List<AdditionalMeal> {
        val json = prefs.getString("additional_meals", null) ?: return emptyList()
        return try {
            Json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(AdditionalMeal.serializer()), json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveHandledMeals(handled: Map<String, Set<String>>) {
        val json = Json.encodeToString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), handled)
        prefs.edit { putString("handled_meals", json) }
    }

    fun getHandledMeals(): Map<String, Set<String>> {
        val json = prefs.getString("handled_meals", null) ?: return emptyMap()
        return try {
            Json.decodeFromString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), json)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveConsumedCalories(calories: Map<String, Int>) {
        val json = Json.encodeToString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), serializer<Int>()), calories)
        prefs.edit { putString("consumed_calories", json) }
    }

    fun getConsumedCalories(): Map<String, Int> {
        val json = prefs.getString("consumed_calories", null) ?: return emptyMap()
        return try {
            Json.decodeFromString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), serializer<Int>()), json)
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
