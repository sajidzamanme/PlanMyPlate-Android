package com.teamconfused.planmyplate.di

import com.teamconfused.planmyplate.domain.usecase.CreateMealPlanUseCase
import com.teamconfused.planmyplate.domain.usecase.FilterRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GenerateMealPlanUseCase
import com.teamconfused.planmyplate.domain.usecase.GenerateRecipeUseCase
import com.teamconfused.planmyplate.domain.usecase.GetAllRecipesUseCase
import com.teamconfused.planmyplate.domain.usecase.GetTodaysMealsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetTodaysMealsUseCase(get()) }
    factory { GenerateRecipeUseCase(get()) }
    factory { GenerateMealPlanUseCase(get()) }
    factory { GetAllRecipesUseCase(get()) }
    factory { FilterRecipesUseCase(get()) }
    factory { CreateMealPlanUseCase(get()) }
    factory { com.teamconfused.planmyplate.domain.usecase.GetRecipeUseCase(get()) }
}
