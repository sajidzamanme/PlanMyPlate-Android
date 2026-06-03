package com.teamconfused.planmyplate.domain.model.recipe

import androidx.compose.ui.graphics.Color

data class MealStyle(
    val emoji: String,
    val accent: Color,
    val surface: Color,
    val onAccent: Color? = null,
)

val mealStyles = mapOf(
    "Breakfast" to MealStyle("☀️", Color(0xFFE67E22), Color(0xFFFFF8F0), Color(0xFF7B3F00)),
    "Lunch"     to MealStyle("🌿", Color(0xFF27AE60), Color(0xFFF0FAF4), Color(0xFF145A32)),
    "Dinner"    to MealStyle("🌙", Color(0xFF6C5CE7), Color(0xFFF5F3FF), Color(0xFF2D1B69)),
)
