package com.teamconfused.planmyplate.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.teamconfused.planmyplate.R
import com.teamconfused.planmyplate.ui.navigation.BottomNavItem
import com.teamconfused.planmyplate.ui.navigation.Screen

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onMoreClick: () -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", R.drawable.home_icon, Screen.Home),
        BottomNavItem("Meal Plan", R.drawable.list_icon, Screen.MealPlan),
        BottomNavItem("Groceries", R.drawable.shopping_icon, Screen.Groceries),
        BottomNavItem("More", R.drawable.ic_more, Screen.Settings)
    )

    // Build a set of qualified route names for matching
    val bottomNavRoutes = items.map { it.screen::class.qualifiedName }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = if (item.label == "More") {
                currentRoute?.contains(Screen.Settings::class.qualifiedName ?: "") == true ||
                currentRoute?.contains(Screen.Favorites::class.qualifiedName ?: "") == true
            } else {
                currentRoute?.contains(
                    item.screen::class.qualifiedName ?: ""
                ) == true
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = isSelected,
                onClick = {
                    if (item.label == "More") {
                        onMoreClick()
                    } else if (!isSelected) {
                        navController.navigate(item.screen) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
