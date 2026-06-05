package com.teamconfused.planmyplate.di

import com.teamconfused.planmyplate.data.repository.*
import com.teamconfused.planmyplate.domain.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<MealPlanRepository> { MealPlanRepositoryImpl(get()) }
    single<AiRepository> { AiRepositoryImpl(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }
    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }
    single<RatingRepository> { RatingRepositoryImpl(get()) }
    single<ExpiryRepository> { ExpiryRepositoryImpl(get()) }
    single<GroceryRepository> { GroceryRepositoryImpl(get()) }
    single<InventoryRepository> { InventoryRepositoryImpl(get()) }
}
