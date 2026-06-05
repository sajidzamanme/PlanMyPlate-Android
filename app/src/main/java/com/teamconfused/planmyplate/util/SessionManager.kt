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

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        isLenient = true
    }

    fun saveUserId(userId: Int) {
        prefs.edit(commit = true) { putInt("user_id", userId) }
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1 && getAuthToken() != null
    }
    
    fun setHasMealPlans(hasMealPlans: Boolean) {
        prefs.edit(commit = true) { putBoolean("has_meal_plans", hasMealPlans) }
    }
    
    fun hasMealPlans(): Boolean {
        return prefs.getBoolean("has_meal_plans", false)
    }

    fun clearSession() {
        prefs.edit(commit = true) {
            remove("user_id")
            remove("auth_token")
            remove("user_preferences")
            remove("has_meal_plans")
            remove("additional_meals")
            remove("handled_meals")
            remove("cooked_meals")
            remove("skipped_meals")
            remove("consumed_calories")
            remove("expiry_warning_days")
            clear()
        }
    }

    fun saveAuthToken(token: String) {
        prefs.edit(commit = true) { putString("auth_token", token) }
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveUserPreferences(preferences: UserPreferences) {
        try {
            val json = jsonConfig.encodeToString(UserPreferences.serializer(), preferences)
            prefs.edit(commit = true) { putString("user_preferences", json) }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to encode user preferences", e)
        }
    }

    fun getUserPreferences(): UserPreferences {
        val json = prefs.getString("user_preferences", null)
        return if (json != null) {
            try {
                jsonConfig.decodeFromString(UserPreferences.serializer(), json)
            } catch (e: Exception) {
                android.util.Log.e("SessionManager", "Failed to decode preferences JSON: $json", e)
                UserPreferences()
            }
        } else {
            UserPreferences()
        }
    }

    // --- Local Sandbox Storage ---

    fun saveAdditionalMeals(meals: List<AdditionalMeal>) {
        try {
            val json = jsonConfig.encodeToString(kotlinx.serialization.builtins.ListSerializer(AdditionalMeal.serializer()), meals)
            prefs.edit(commit = true) { putString("additional_meals", json) }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to encode additional meals", e)
        }
    }

    fun getAdditionalMeals(): List<AdditionalMeal> {
        val json = prefs.getString("additional_meals", null) ?: return emptyList()
        return try {
            jsonConfig.decodeFromString(kotlinx.serialization.builtins.ListSerializer(AdditionalMeal.serializer()), json)
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to decode additional meals", e)
            emptyList()
        }
    }

    fun saveHandledMeals(handled: Map<String, Set<String>>) {
        try {
            val json = jsonConfig.encodeToString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), handled)
            prefs.edit(commit = true) { putString("handled_meals", json) }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to encode handled meals", e)
        }
    }

    fun getHandledMeals(): Map<String, Set<String>> {
        val json = prefs.getString("handled_meals", null) ?: return emptyMap()
        return try {
            jsonConfig.decodeFromString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), json)
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to decode handled meals", e)
            emptyMap()
        }
    }

    fun saveCookedMeals(cooked: Map<String, Set<String>>) {
        try {
            val json = jsonConfig.encodeToString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), cooked)
            prefs.edit(commit = true) { putString("cooked_meals", json) }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to encode cooked meals", e)
        }
    }

    fun getCookedMeals(): Map<String, Set<String>> {
        val json = prefs.getString("cooked_meals", null) ?: return emptyMap()
        return try {
            jsonConfig.decodeFromString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), json)
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to decode cooked meals", e)
            emptyMap()
        }
    }

    fun saveSkippedMeals(skipped: Map<String, Set<String>>) {
        try {
            val json = jsonConfig.encodeToString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), skipped)
            prefs.edit(commit = true) { putString("skipped_meals", json) }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to encode skipped meals", e)
        }
    }

    fun getSkippedMeals(): Map<String, Set<String>> {
        val json = prefs.getString("skipped_meals", null) ?: return emptyMap()
        return try {
            jsonConfig.decodeFromString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), kotlinx.serialization.builtins.SetSerializer(serializer<String>())), json)
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to decode skipped meals", e)
            emptyMap()
        }
    }

    fun saveConsumedCalories(calories: Map<String, Int>) {
        try {
            val json = jsonConfig.encodeToString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), serializer<Int>()), calories)
            prefs.edit(commit = true) { putString("consumed_calories", json) }
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to encode consumed calories", e)
        }
    }

    fun getConsumedCalories(): Map<String, Int> {
        val json = prefs.getString("consumed_calories", null) ?: return emptyMap()
        return try {
            jsonConfig.decodeFromString(kotlinx.serialization.builtins.MapSerializer(serializer<String>(), serializer<Int>()), json)
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to decode consumed calories", e)
            emptyMap()
        }
    }

    fun saveExpiryWarningDays(days: Int) {
        prefs.edit(commit = true) { putInt("expiry_warning_days", days) }
    }

    fun getExpiryWarningDays(): Int {
        return prefs.getInt("expiry_warning_days", 10)
    }
}
