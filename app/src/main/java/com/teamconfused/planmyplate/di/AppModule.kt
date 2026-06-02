package com.teamconfused.planmyplate.di

import com.teamconfused.planmyplate.network.RetrofitClient
import com.teamconfused.planmyplate.util.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Session Manager
    single { SessionManager(androidContext()) }

    // Network
    // Using existing RetrofitClient for now, but should ideally move configuration here
    single { RetrofitClient.authService }
    single { RetrofitClient.userService }
    single { RetrofitClient.userPreferencesService }
    single { RetrofitClient.recipeService }
    single { RetrofitClient.ingredientService }
    single { RetrofitClient.mealPlanService }
    single { RetrofitClient.groceryListService }
    single { RetrofitClient.inventoryService }
    single { RetrofitClient.aiService }
    single { RetrofitClient.expiryService }
}
