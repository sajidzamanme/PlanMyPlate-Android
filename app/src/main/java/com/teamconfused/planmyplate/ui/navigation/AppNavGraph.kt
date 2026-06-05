package com.teamconfused.planmyplate.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.teamconfused.planmyplate.ui.components.BottomNavigationBar
import com.teamconfused.planmyplate.ui.screens.AddRecipeScreen
import com.teamconfused.planmyplate.ui.screens.ExpiryAlertsScreen
import com.teamconfused.planmyplate.ui.screens.FavoritesScreen
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
import com.teamconfused.planmyplate.ui.screens.ProfileScreen
import com.teamconfused.planmyplate.ui.screens.EditProfileScreen
import com.teamconfused.planmyplate.ui.viewmodels.AddRecipeViewModel
import com.teamconfused.planmyplate.ui.viewmodels.ProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import com.teamconfused.planmyplate.ui.viewmodels.ExpiryViewModel
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
    Screen.Favorites::class.qualifiedName,
    Screen.Groceries::class.qualifiedName,
    Screen.Settings::class.qualifiedName
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(navController: NavHostController) {
    val sessionManager: SessionManager = koinInject()
    val startDestination: Screen = if (sessionManager.isLoggedIn()) {
        val prefs = sessionManager.getUserPreferences()
        val hasPreferences = prefs.prefId != null || prefs.diet != null || prefs.budget != null
        if (hasPreferences) Screen.Home else Screen.PreferenceSelection
    } else {
        Screen.Welcome
    }

    // Determine if the bottom bar should be shown based on the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = navBackStackEntry?.destination?.route?.let { route ->
        bottomNavRouteNames.any { name -> name != null && route.contains(name) }
    } ?: false

    var showMoreBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    onMoreClick = { showMoreBottomSheet = true }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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
                    onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword) },
                    onDismissError = viewModel::clearError
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
                        viewModel.onSignupClick { hasPreferences ->
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
                    onBackClick = { navController.popBackStack() },
                    onDismissError = viewModel::clearError
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
                    onBackClick = { navController.popBackStack() },
                    onDismissError = viewModel::clearError
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
                    onGenderSelected = viewModel::onGenderSelected,
                    onHeightChanged = viewModel::onHeightChanged,
                    onWeightChanged = viewModel::onWeightChanged,
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
                val expiryViewModel: ExpiryViewModel = koinViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    expiryViewModel = expiryViewModel,
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
                    },
                    onNavigateToExpiryAlerts = {
                        navController.navigate(Screen.ExpiryAlerts)
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
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
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

            composable<Screen.Favorites> {
                FavoritesScreen(
                    onNavigateToRecipeDetails = { recipeId ->
                        navController.navigate(
                            Screen.RecipeDetails(recipeId = recipeId)
                        )
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.ExpiryAlerts> {
                ExpiryAlertsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Screen.AddRecipe> {
                val addRecipeViewModel: AddRecipeViewModel = koinViewModel()
                AddRecipeScreen(
                    viewModel = addRecipeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Screen.Profile> {
                val profileViewModel: ProfileViewModel = koinViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onUpdateProfileClick = {
                        navController.navigate(Screen.PreferenceSelection)
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditProfile)
                    },
                    onLogoutClick = {
                        profileViewModel.logout()
                        navController.navigate(Screen.Welcome) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Screen.EditProfile> {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        if (showMoreBottomSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showMoreBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 8.dp)
                ) {
                    Text(
                        text = "More Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))

                    ListItem(
                        headlineContent = { Text("Profile", fontWeight = FontWeight.Medium) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            showMoreBottomSheet = false
                            navController.navigate(Screen.Profile)
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Favourites", fontWeight = FontWeight.Medium) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = com.teamconfused.planmyplate.R.drawable.ic_heart),
                                contentDescription = "Favourites",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            showMoreBottomSheet = false
                            navController.navigate(Screen.Favorites) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Settings", fontWeight = FontWeight.Medium) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = com.teamconfused.planmyplate.R.drawable.settings_icon),
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            showMoreBottomSheet = false
                            navController.navigate(Screen.Settings) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
