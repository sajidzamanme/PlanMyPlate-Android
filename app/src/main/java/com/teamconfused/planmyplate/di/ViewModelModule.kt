package com.teamconfused.planmyplate.di

import com.teamconfused.planmyplate.ui.viewmodels.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { SignupViewModel(get(), get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { PreferenceSelectionViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { MealPlanViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { AddRecipeViewModel(get(), get()) }
    viewModel { RecipeViewModel(get(), get(), get(), get()) }
    viewModel { GroceryViewModel(get(), get(), get(), get()) }
    viewModel { InventoryViewModel(get(), get()) }
}
