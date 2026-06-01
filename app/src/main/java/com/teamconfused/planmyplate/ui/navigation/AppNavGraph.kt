package com.teamconfused.planmyplate.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.teamconfused.planmyplate.ui.components.BottomNavigationBar
import com.teamconfused.planmyplate.ui.screens.AddRecipeScreen
import com.teamconfused.planmyplate.ui.screens.ForgotPasswordScreen
import com.teamconfused.planmyplate.ui.screens.GroceriesScreen
import com.teamconfused.planmyplate.ui.screens.HomeScreen
import com.teamconfused.planmyplate.ui.screens.InventoryScreen
import com.teamconfused.planmyplate.ui.screens.LoginScreen
import com.teamconfused.planmyplate.ui.screens.MealPlanScreen
import com.teamconfused.planmyplate.ui.screens.PreferenceSelectionScreen
import com.teamconfused.planmyplate.ui.screens.RecipeDetailsScreen
import com.teamconfused.planmyplate.ui.screens.RecipeSelectionScreen
import com.teamconfused.planmyplate.ui.screens.SettingsScreen
import com.teamconfused.planmyplate.ui.screens.SignupScreen
import com.teamconfused.planmyplate.ui.screens.WelcomeScreen
import com.teamconfused.planmyplate.ui.viewmodels.ForgotPasswordViewModel
import com.teamconfused.planmyplate.ui.viewmodels.LoginViewModel
import com.teamconfused.planmyplate.ui.viewmodels.MealPlanViewModel
import com.teamconfused.planmyplate.ui.viewmodels.PreferenceSelectionViewModel
import com.teamconfused.planmyplate.ui.viewmodels.SettingsViewModel
import com.teamconfused.planmyplate.ui.viewmodels.SignupViewModel
import com.teamconfused.planmyplate.util.SessionManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Set of bottom-nav route qualified names used to determine when the bottom bar is visible.
 */
private val bottomNavRouteNames = setOf(
    Screen.Home::class.qualifiedName,
    Screen.MealPlan::class.qualifiedName,
    Screen.Groceries::class.qualifiedName,
    Screen.Settings::class.qualifiedName
)

@Composable
fun AppNavGraph(navController: NavHostController) {
    val sessionManager: SessionManager = koinInject()
    val startDestination: Screen = if (sessionManager.isLoggedIn()) Screen.Home else Screen.Welcome

    // Determine if the bottom bar should be shown based on the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = navBackStackEntry?.destination?.route?.let { route ->
        bottomNavRouteNames.any { name -> name != null && route.contains(name) }
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ═══════════════════════════════════════════════════════════════════
            // AUTH FLOW (no bottom bar)
            // ═══════════════════════════════════════════════════════════════════

            composable<Screen.Welcome> {
                WelcomeScreen(
                    onGetStartedClick = { navController.navigate(Screen.Signup) },
                    onLoginClick = { navController.navigate(Screen.Login) }
                )
            }

            composable<Screen.Login> {
                val viewModel: LoginViewModel = koinViewModel()
                val uiState by viewModel.uiState.collectAsState()

                LoginScreen(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onLoginClick = {
                        viewModel.onLoginClick { hasPreferences ->
                            if (hasPreferences) {
                                navController.navigate(Screen.Home) {
                                    popUpTo(Screen.Welcome) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.PreferenceSelection) {
                                    popUpTo(Screen.Welcome) { inclusive = true }
                                }
                            }
                        }
                    },
                    onSignupClick = {
                        navController.navigate(Screen.Signup) {
                            popUpTo(Screen.Welcome) { inclusive = false }
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                    onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword) }
                )
            }

            composable<Screen.Signup> {
                val viewModel: SignupViewModel = koinViewModel()
                val uiState by viewModel.uiState.collectAsState()

                SignupScreen(
                    uiState = uiState,
                    onFirstNameChange = viewModel::onFirstNameChange,
                    onLastNameChange = viewModel::onLastNameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onDateOfBirthChange = viewModel::onDateOfBirthChange,
                    onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
                    onLoginClick = {
                        navController.navigate(Screen.Login) {
                            popUpTo(Screen.Welcome) { inclusive = false }
                        }
                    },
                    onSignupClick = {
                        viewModel.onSignupClick {
                            navController.navigate(Screen.PreferenceSelection) {
                                popUpTo(Screen.Welcome) { inclusive = true }
                            }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.ForgotPassword> {
                val viewModel: ForgotPasswordViewModel = koinViewModel()
                val uiState by viewModel.uiState.collectAsState()

                ForgotPasswordScreen(
                    uiState = uiState,
                    onEmailChange = viewModel::onEmailChange,
                    onCodeChange = viewModel::onCodeChange,
                    onNewPasswordChange = viewModel::onNewPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onSendCodeClick = viewModel::onSendCodeClick,
                    onVerifyCodeClick = viewModel::onVerifyCodeClick,
                    onResetPasswordClick = viewModel::onResetPasswordClick,
                    onLoginClick = {
                        navController.navigate(Screen.Login) {
                             popUpTo(Screen.Welcome) { inclusive = false }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.PreferenceSelection> {
                val viewModel: PreferenceSelectionViewModel = koinViewModel()
                val uiState by viewModel.uiState.collectAsState()

                PreferenceSelectionScreen(
                    uiState = uiState,
                    onDietSelected = viewModel::onDietSelected,
                    onAllergyToggled = viewModel::onAllergyToggled,
                    onDislikeToggled = viewModel::onDislikeToggled,
                    onServingsSelected = viewModel::onServingsSelected,
                    onBudgetSelected = viewModel::onBudgetSelected,
                    onNextStep = {
                        viewModel.onNextStep {
                            navController.navigate(Screen.Home) {
                                popUpTo(Screen.PreferenceSelection) { inclusive = true }
                            }
                        }
                    },
                    onBackClick = {
                        viewModel.onPreviousStep {
                            navController.popBackStack()
                        }
                    }
                )
            }

            // ═══════════════════════════════════════════════════════════════════
            // MAIN TABS (bottom bar visible)
            // ═══════════════════════════════════════════════════════════════════

            composable<Screen.Home> {
                val homeViewModel: com.teamconfused.planmyplate.ui.viewmodels.HomeViewModel = koinViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToMealPlan = {
                        navController.navigate(Screen.MealPlan) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToRecipeDetails = { recipeId, fromDashboard, mealType ->
                        navController.navigate(
                            Screen.RecipeDetails(
                                recipeId = recipeId,
                                fromDashboard = fromDashboard,
                                mealType = mealType
                            )
                        )
                    }
                )
            }

            composable<Screen.MealPlan> {
                val mealPlanViewModel: MealPlanViewModel = koinViewModel()
                MealPlanScreen(
                    viewModel = mealPlanViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    },
                    onNavigateToRecipeDetails = { recipeId ->
                        navController.navigate(
                            Screen.RecipeDetails(recipeId = recipeId, readOnly = true)
                        )
                    },
                    onNavigateToRecipeSelection = { mealType ->
                        navController.navigate(Screen.RecipeSelection(mealType = mealType))
                    }
                )
            }

            composable<Screen.Groceries> {
                GroceriesScreen(
                    onNavigateToInventory = {
                        navController.navigate(Screen.Inventory)
                    }
                )
            }

            composable<Screen.Inventory> {
                InventoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Screen.Settings> {
                val settingsViewModel: SettingsViewModel = koinViewModel()
                SettingsScreen(
                    onLogoutClick = {
                        settingsViewModel.logout()
                        navController.navigate(Screen.Welcome) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onUpdatePreferencesClick = {
                        navController.navigate(Screen.PreferenceSelection)
                    }
                )
            }

            // ═══════════════════════════════════════════════════════════════════
            // DETAIL SCREENS (no bottom bar)
            // ═══════════════════════════════════════════════════════════════════

            composable<Screen.RecipeDetails> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.RecipeDetails>()

                // For selection mode, we need the MealPlanViewModel scoped to the MealPlan backstack entry
                val mealPlanViewModel: MealPlanViewModel? = if (route.isSelectionMode) {
                    val mealPlanEntry = remember(backStackEntry) {
                        try {
                            navController.getBackStackEntry<Screen.MealPlan>()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    mealPlanEntry?.let { koinViewModel(viewModelStoreOwner = it) }
                } else null

                // For dashboard mode, we need HomeViewModel scoped to the Home backstack entry
                val homeViewModel: com.teamconfused.planmyplate.ui.viewmodels.HomeViewModel? = if (route.fromDashboard) {
                    val homeEntry = remember(backStackEntry) {
                        try {
                            navController.getBackStackEntry<Screen.Home>()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    homeEntry?.let { koinViewModel(viewModelStoreOwner = it) }
                } else null

                val mealPlanUiState = mealPlanViewModel?.uiState?.collectAsState()
                val currentMealType = route.mealType ?: "Breakfast"
                val isAdded = if (route.isSelectionMode && mealPlanUiState != null) {
                    mealPlanUiState.value.selectedRecipes[currentMealType]?.any { it.recipeId == route.recipeId } == true
                } else {
                    true
                }

                RecipeDetailsScreen(
                    recipeId = route.recipeId,
                    isInitiallyAdded = isAdded,
                    showControls = !route.readOnly,
                    fromDashboard = route.fromDashboard,
                    mealType = route.mealType,
                    onNavigateBack = { navController.popBackStack() },
                    onToggleRecipe = { recipe ->
                        if (route.isSelectionMode) {
                            mealPlanViewModel?.toggleRecipe(currentMealType, recipe)
                        }
                    },
                    onCooked = { type, calories, id ->
                        homeViewModel?.markAsCooked(type, calories, id)
                    },
                    onSkip = { id ->
                        homeViewModel?.skipMeal(route.mealType, id)
                    }
                )
            }

            composable<Screen.RecipeSelection> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.RecipeSelection>()

                // Get MealPlanViewModel scoped to the MealPlan backstack entry
                val mealPlanEntry = remember(backStackEntry) {
                    try {
                        navController.getBackStackEntry<Screen.MealPlan>()
                    } catch (e: Exception) {
                        null
                    }
                }
                val mealPlanViewModel: MealPlanViewModel = if (mealPlanEntry != null) {
                    koinViewModel(viewModelStoreOwner = mealPlanEntry)
                } else {
                    koinViewModel()
                }

                RecipeSelectionScreen(
                    mealType = route.mealType,
                    viewModel = mealPlanViewModel,
                    onBackClick = { navController.popBackStack() },
                    onRecipeDetailsClick = { recipeId ->
                        navController.navigate(
                            Screen.RecipeDetails(
                                recipeId = recipeId,
                                isSelectionMode = true,
                                mealType = route.mealType
                            )
                        )
                    }
                )
            }
        }
    }
}
