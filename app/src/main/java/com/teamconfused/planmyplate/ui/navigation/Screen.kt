package com.teamconfused.planmyplate.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    // Auth flow
    @Serializable
    data object Welcome : Screen()

    @Serializable
    data object Login : Screen()

    @Serializable
    data object Signup : Screen()

    @Serializable
    data object ForgotPassword : Screen()

    @Serializable
    data object PreferenceSelection : Screen()

    // Main tabs (bottom nav)
    @Serializable
    data object Home : Screen()

    @Serializable
    data object MealPlan : Screen()

    @Serializable
    data object Groceries : Screen()

    @Serializable
    data object Inventory : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Favorites : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object EditProfile : Screen()

    @Serializable
    data object ExpiryAlerts : Screen()

    @Serializable
    data object AddRecipe : Screen()

    // Detail screens
    @Serializable
    data class RecipeDetails(
        val recipeId: Int,
        val isSelectionMode: Boolean = false,
        val mealType: String? = null,
        val readOnly: Boolean = false,
        val fromDashboard: Boolean = false
    ) : Screen()

    @Serializable
    data class RecipeSelection(
        val mealType: String
    ) : Screen()
}
