package com.teamconfused.planmyplate.di

import com.teamconfused.planmyplate.data.repository.AiRepositoryImpl
import com.teamconfused.planmyplate.data.repository.MealPlanRepositoryImpl
import com.teamconfused.planmyplate.data.repository.RecipeRepositoryImpl
import com.teamconfused.planmyplate.domain.repository.AiRepository
import com.teamconfused.planmyplate.domain.repository.MealPlanRepository
import com.teamconfused.planmyplate.domain.repository.RecipeRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<MealPlanRepository> { MealPlanRepositoryImpl(get()) }
    single<AiRepository> { AiRepositoryImpl(get()) }
    single<RecipeRepository> { RecipeRepositoryImpl(get()) }
}
