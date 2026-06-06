package com.teamconfused.planmyplate.data.mapper

import com.teamconfused.planmyplate.data.model.GroceryListDto
import com.teamconfused.planmyplate.data.model.GroceryListItemDto
import com.teamconfused.planmyplate.data.model.IngredientDto
import com.teamconfused.planmyplate.data.model.IngredientRefDto
import com.teamconfused.planmyplate.data.model.InventoryDto
import com.teamconfused.planmyplate.data.model.InventoryItemDto
import com.teamconfused.planmyplate.data.model.MealPlanDto
import com.teamconfused.planmyplate.data.model.MealSlotDto
import com.teamconfused.planmyplate.data.model.RecipeIngredientResponse
import com.teamconfused.planmyplate.data.model.RecipeResponse
import com.teamconfused.planmyplate.data.model.AllergyDto
import com.teamconfused.planmyplate.data.model.DietDto
import com.teamconfused.planmyplate.data.model.UserDto
import com.teamconfused.planmyplate.data.model.UserPreferencesResponse
import com.teamconfused.planmyplate.data.model.ExpiryItemResponse
import com.teamconfused.planmyplate.data.model.SoonToExpireResponse
import com.teamconfused.planmyplate.domain.model.GroceryList
import com.teamconfused.planmyplate.domain.model.GroceryListItem
import com.teamconfused.planmyplate.domain.model.Ingredient
import com.teamconfused.planmyplate.domain.model.Inventory
import com.teamconfused.planmyplate.domain.model.InventoryItem
import com.teamconfused.planmyplate.domain.model.MealPlan
import com.teamconfused.planmyplate.domain.model.MealSlot
import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.model.Allergy
import com.teamconfused.planmyplate.domain.model.Diet
import com.teamconfused.planmyplate.domain.model.User
import com.teamconfused.planmyplate.domain.model.UserPreferences
import com.teamconfused.planmyplate.domain.model.ExpiryItem
import com.teamconfused.planmyplate.domain.model.SoonToExpireResult
import com.teamconfused.planmyplate.data.model.UserFavoriteResponse
import com.teamconfused.planmyplate.domain.model.UserFavorite
import com.teamconfused.planmyplate.data.model.RecipeRatingResponse
import com.teamconfused.planmyplate.data.model.RecipeRatingSummary as RatingSummaryDto
import com.teamconfused.planmyplate.domain.model.RecipeRating
import com.teamconfused.planmyplate.domain.model.RecipeRatingSummary as RatingSummaryDomain

// Recipe Mappers
fun RecipeResponse.toDomain(): Recipe {
    return Recipe(
        recipeId = this.recipeId,
        name = this.name,
        description = this.description ?: "",
        calories = this.calories ?: 0,
        protein = this.protein,
        carbs = this.carbs,
        fat = this.fat,
        fiber = this.fiber,
        prepTime = this.prepTime,
        cookTime = this.cookTime,
        servings = this.servings,
        instructions = this.instructions,
        ingredients = this.recipeIngredients?.map { it.toIngredientString() },
        imageUrl = this.imageUrl
    )
}

fun RecipeIngredientResponse.toIngredientString(): String {
    val qty = if (this.quantity != null && this.quantity > 0) "${this.quantity} " else ""
    val unitStr = if (!this.unit.isNullOrBlank()) "${this.unit} " else ""
    val name = this.ingredient?.name ?: "Unknown Ingredient"
    return "$qty$unitStr$name".trim()
}

// User Mappers
fun UserDto.toDomain(): User {
    return User(
        userId = this.userId,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phone = this.phone,
        dateOfBirth = this.dateOfBirth,
        age = this.age,
        weight = this.weight,
        budget = this.budget
    )
}

fun UserPreferencesResponse.toDomain(): UserPreferences {
    return UserPreferences(
        prefId = this.prefId,
        userId = this.userId,
        diets = this.diets,
        allergies = this.allergies,
        dislikes = this.dislikes,
        budget = this.budget,
        height = this.height,
        weight = this.weight,
        gender = this.gender,
        bmi = this.bmi,
        bmiCategory = this.bmiCategory
    )
}

fun DietDto.toDomain(): Diet {
    return Diet(
        dietId = this.dietId,
        dietName = this.dietName
    )
}

fun AllergyDto.toDomain(): Allergy {
    return Allergy(
        allergyId = this.allergyId,
        allergyName = this.allergyName
    )
}

// Ingredient Mappers
fun IngredientDto.toDomain(): Ingredient {
    return Ingredient(
        ingId = this.ingId,
        name = this.name,
        price = this.price?.toFloat()
    )
}

fun IngredientRefDto.toDomain(): Ingredient {
    return Ingredient(
        ingId = this.ingId,
        name = this.name ?: "Unknown Ingredient"
    )
}

// Meal Plan Mappers
fun MealPlanDto.toDomain(): MealPlan {
    return MealPlan(
        mpId = this.mpId,
        userId = this.userId,
        startDate = this.startDate,
        duration = this.duration,
        status = this.status,
        slots = this.slots?.map { it.toDomain() }
    )
}

fun MealSlotDto.toDomain(): MealSlot {
    return MealSlot(
        slotId = this.id,
        mealType = this.mealType,
        dayNumber = this.dayNumber,
        recipe = this.recipe?.toDomain()
    )
}

// Grocery List Mappers
fun GroceryListDto.toDomain(): GroceryList {
    return GroceryList(
        listId = this.listId,
        userId = this.userId,
        dateCreated = this.dateCreated,
        status = this.status,
        items = this.items?.map { it.toDomain() }
    )
}

fun GroceryListItemDto.toDomain(): GroceryListItem {
    return GroceryListItem(
        id = this.id,
        ingredient = this.ingredient?.toDomain(),
        quantity = this.quantity,
        unit = this.unit
    )
}

// Inventory Mappers
fun InventoryDto.toDomain(): Inventory {
    return Inventory(
        invId = this.invId,
        userId = this.userId,
        lastUpdate = this.lastUpdate,
        items = this.items?.map { it.toDomain() }
    )
}

fun InventoryItemDto.toDomain(): InventoryItem {
    return InventoryItem(
        itemId = this.itemId,
        quantity = this.quantity,
        unit = this.unit,
        dateAdded = this.dateAdded,
        expiryDate = this.expiryDate,
        ingredient = this.ingredient?.toDomain()
    )
}

// Expiry Mappers
fun ExpiryItemResponse.toDomain(): ExpiryItem {
    return ExpiryItem(
        itemId = this.itemId,
        productName = this.productName,
        expiryDate = this.expiryDate,
        dateAdded = this.dateAdded,
        quantity = this.quantity,
        unit = this.unit,
        daysUntilExpiry = this.daysUntilExpiry,
        isExpired = this.isExpired
    )
}

fun SoonToExpireResponse.toDomain(): SoonToExpireResult {
    return SoonToExpireResult(
        thresholdDays = this.thresholdDays,
        totalCount = this.totalCount,
        expiredCount = this.expiredCount,
        items = this.items?.map { it.toDomain() }
    )
}

fun UserFavoriteResponse.toDomain(): UserFavorite {
    return UserFavorite(
        id = this.id,
        userId = this.userId,
        recipeId = this.recipeId,
        recipe = this.recipe?.toDomain(),
        createdAt = this.createdAt
    )
}

fun RecipeRatingResponse.toDomain(): RecipeRating {
    return RecipeRating(
        ratingId = this.ratingId,
        userId = this.userId,
        recipeId = this.recipeId,
        rating = this.rating,
        review = this.review,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun RatingSummaryDto.toDomain(): RatingSummaryDomain {
    return RatingSummaryDomain(
        recipeId = this.recipeId,
        averageRating = this.averageRating,
        totalRatings = this.totalRatings
    )
}
