package com.teamconfused.planmyplate.ui.navigation

import androidx.annotation.DrawableRes

data class BottomNavItem(
    val label: String,
    @DrawableRes val icon: Int,
    val screen: Screen
)
