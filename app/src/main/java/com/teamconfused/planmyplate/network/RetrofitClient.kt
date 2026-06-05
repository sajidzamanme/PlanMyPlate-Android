package com.teamconfused.planmyplate.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitClient {
    private const val BASE_URL = "https://planmyplate-python-production-4a10.up.railway.app" // 10.0.2.2 is localhost for Android Emulator

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authService: AuthService by lazy { retrofit.create(AuthService::class.java) }
    val userService: UserService by lazy { retrofit.create(UserService::class.java) }
    val userPreferencesService: UserPreferencesService by lazy { retrofit.create(UserPreferencesService::class.java) }
    val recipeService: RecipeService by lazy { retrofit.create(RecipeService::class.java) }
    val ingredientService: IngredientService by lazy { retrofit.create(IngredientService::class.java) }
    val mealPlanService: MealPlanService by lazy { retrofit.create(MealPlanService::class.java) }
    val groceryListService: GroceryListService by lazy { retrofit.create(GroceryListService::class.java) }
    val inventoryService: InventoryService by lazy { retrofit.create(InventoryService::class.java) }
    val aiService: AiService by lazy { retrofit.create(AiService::class.java) }
    val expiryService: ExpiryService by lazy { retrofit.create(ExpiryService::class.java) }
    val favoriteService: FavoriteService by lazy { retrofit.create(FavoriteService::class.java) }
    val ratingService: RatingService by lazy { retrofit.create(RatingService::class.java) }
}
