# PlanMyPlate API Migration & Clean Architecture Alignment Plan

This document outlines the comprehensive migration and refactoring plan to align the PlanMyPlate Android application with the updated FastAPI (Python) backend API. 

## User Decisions (Incorporated)

> [!IMPORTANT]
> **Custom Recipe Ingredients**: Restricted to verified database ingredients. We will implement search-and-select autocomplete dialogs to match ingredient names to `ingId`s.
> 
> **Favourites Tab**: Added as a dedicated bottom navigation bar tab (Home, Meal Plan, Favourites, Groceries, Settings).

> [!NOTE]
> We will resolve architectural inconsistencies where ViewModels directly access network services, wrapping them in clean repositories and use cases.

---

## Phase 1: DTO & Domain Model Realignment

Align DTO naming schemes and models with the camelCase properties returned by the FastAPI backend, and add new fields (such as macros and preference values).

### 1. `AuthDtos.kt`
- **Modify** [AuthDtos.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/data/model/AuthDtos.kt)
- **Changes**:
  - `SignupRequest`: Update `@SerialName` of `firstName`, `lastName`, and `dateOfBirth` to match camelCase (`firstName`, `lastName`, `dateOfBirth`).
  - `AuthResponse`: Update `@SerialName` of `accessToken`, `tokenType`, `firstName`, `lastName`, `userId`, `dateOfBirth` to match camelCase (`access_token`, `token_type` are snake_case, but `firstName`, `lastName`, `userId`, `dateOfBirth` are camelCase in the API response).
  - `ResetPasswordRequest`: Update `@SerialName` of `resetToken` and `newPassword` to match camelCase (`resetToken`, `newPassword`).

### 2. `UserDtos.kt`
- **Modify** [UserDtos.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/data/model/UserDtos.kt)
- **Changes**:
  - `UserDto`: Update `@SerialName` of `userId`, `firstName`, `lastName`, `dateOfBirth` to camelCase.
  - `UpdateUserRequest`: Update `@SerialName` of `firstName`, `lastName`, `dateOfBirth` to camelCase.
  - `UserPreferencesRequest`: Update `@SerialName("user_id")` to `userId`. Add fields: `height` (Float), `weight` (Float), `gender` (String). Remove `servings` as it is not part of the preferences endpoint parameters.
  - `UserPreferencesResponse`: Update `@SerialName` of `prefId` and `userId` to camelCase. Add fields: `height` (Float), `gender` (String), `bmi` (Double?), `bmi_category` (String?).

### 3. `User` Domain Model
- **Modify** [User.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/domain/model/User.kt)
- **Changes**:
  - Update `UserPreferences` domain model to match preferences changes: add `height`, `gender`, `bmi`, `bmiCategory`.

### 4. `RecipeDtos.kt`
- **Modify** [RecipeDtos.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/data/model/RecipeDtos.kt)
- **Changes**:
  - `RecipeResponse`: Update `@SerialName` of `recipeId`, `prepTime`, `cookTime`, `recipeIngredients`, `imageUrl` to camelCase. Add macro fields: `protein` (Double?), `carbs` (Double?), `fat` (Double?), `fiber` (Double?).
  - `CreateRecipeRequest`: Update `@SerialName` of `prepTime`, `cookTime`, `imageUrl` to camelCase. Add macro fields: `protein`, `carbs`, `fat`, `fiber`.
  - `RecipeIngredientRequest`: Update `@SerialName("ing_id")` to `ingId`.
  - `GenerateRecipeRequest`: Update `@SerialName` of `availableIngredients`, `maxCalories`, `cuisineType`, `dietaryPreference`, `maxCookingTime` to camelCase.

### 5. `Recipe` Domain Model
- **Modify** [Recipe.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/domain/model/Recipe.kt)
- **Changes**:
  - Add macro fields: `protein` (Double?), `carbs` (Double?), `fat` (Double?), `fiber` (Double?).

### 6. `IngredientDtos.kt`
- **Modify** [IngredientDtos.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/data/model/IngredientDtos.kt)
- **Changes**:
  - `IngredientDto`: Update `@SerialName("ing_id")` to `ingId`.
  - `IngredientRefDto`: Update `@SerialName("ing_id")` to `ingId`.
  - Add `TagDto` helper class:
    ```kotlin
    @Serializable
    data class TagDto(
        val tagId: Int,
        val tagName: String
    )
    ```
  - Update `tags` in `IngredientDto` from `List<String>?` to `List<TagDto>?`.

### 7. `MealPlanDtos.kt`
- **Modify** [MealPlanDtos.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/data/model/MealPlanDtos.kt)
- **Changes**:
  - `MealPlanDto`: Update `@SerialName` of `mpId`, `userId`, `startDate` to camelCase.
  - `MealSlotDto`: Update `@SerialName` of `slotIndex`, `mealType`, `dayNumber`, `servingsMultiplier` to camelCase.
  - `CreateMealPlanRequest`: Update `@SerialName` of `recipeIds`, `servingsMultipliers`, `startDate` to camelCase.

### 8. DTO Mappers
- **Modify** [Mappers.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/data/mapper/Mappers.kt)
- **Changes**:
  - `RecipeResponse.toDomain()`: Map new macro fields (`protein`, `carbs`, `fat`, `fiber`).
  - `UserPreferencesResponse.toDomain()`: Map new fields (`height`, `gender`, `bmi`, `bmiCategory`).

---

## Phase 2: Network Service & Repository Layer Realignment

Implement the missing service endpoints, reorganize admin ingredients routes, and create clean repository interfaces and implementations.

### 1. Create New Services
- **NEW** `FavoriteService.kt`
  - Interfaces:
    - `POST api/favorites/{recipe_id}` (Add to favourites)
    - `DELETE api/favorites/{recipe_id}` (Remove from favourites)
    - `GET api/favorites/` (List favourites with `skip` and `limit`)
    - `GET api/favorites/{recipe_id}/status` (Check status)
- **NEW** `RatingService.kt`
  - Interfaces:
    - `POST api/ratings/` (Create or update rating)
    - `GET api/ratings/my/{recipe_id}` (Get user rating)
    - `GET api/ratings/recipe/{recipe_id}` (Get recipe rating summary)
    - `DELETE api/ratings/{recipe_id}` (Delete rating)

### 2. Update Existing Services
- **Modify** [IngredientService.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/network/IngredientService.kt)
  - Change URL paths for POST, PUT, and DELETE to use `/api/admin/ingredients` as specified by Section 15.
  - Remove non-existent `/api/ingredients/price/range` endpoint.
- **Modify** [UserPreferencesService.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/network/UserPreferencesService.kt)
  - Remove deprecated `@GET("api/reference-data/allergies")` and `@GET("api/reference-data/dislikes")` endpoints.
- **Modify** [RecipeService.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/network/RecipeService.kt)
  - Update `getAllRecipes` to support optional query parameters `skip` and `limit`.

### 3. Build Missing Repositories (Clean Architecture)
Create repositories to shield ViewModels from directly injecting Retrofit Services:
- **NEW** `UserPreferencesRepository` & `UserPreferencesRepositoryImpl`
- **NEW** `ExpiryRepository` & `ExpiryRepositoryImpl`
- **NEW** `GroceryRepository` & `GroceryRepositoryImpl`
- **NEW** `InventoryRepository` & `InventoryRepositoryImpl`
- **NEW** `FavoriteRepository` & `FavoriteRepositoryImpl`
- **NEW** `RatingRepository` & `RatingRepositoryImpl`

### 4. Register Dependencies
- **Modify** [RepositoryModule.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/di/RepositoryModule.kt)
  - Bind all new repository implementations to their interfaces.
- **Modify** [AppModule.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/di/AppModule.kt)
  - Register `FavoriteService` and `RatingService` as singletons.
- **Modify** [UseCaseModule.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/di/UseCaseModule.kt)
  - Define use cases for the new features (Favourites, Ratings, Expiry tracking, Preferences, Pantry).

---

## Phase 3: ViewModel & Business Logic Refactoring

Refactor the ViewModels to pull data from clean repositories or use cases instead of invoking Retrofit services directly, and implement logic for the new systems.

### 1. Refactor Existing ViewModels
- **Modify** [PreferenceSelectionViewModel.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/viewmodels/PreferenceSelectionViewModel.kt)
  - Replace direct `UserPreferencesService` dependencies with new Use Cases.
  - Update `loadReferenceData` to load allergies and dislikes from `ingredientService.getAllIngredients()` as specified.
- **Modify** [GroceryViewModel.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/viewmodels/GroceryViewModel.kt)
  - Refactor to use `GroceryRepository` or dedicated Use Cases.
- **Modify** [InventoryViewModel.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/viewmodels/InventoryViewModel.kt)
  - Refactor to use `InventoryRepository`.
  - Optimize `fetchInventory()`: Remove the redundant secondary call to `getInventoryItems()` since the parent `getInventoryForUser()` endpoint already returns the item list.
- **Modify** [RecipeViewModel.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/viewmodels/RecipeViewModel.kt)
  - Add state flows and methods for Favourite toggles (`addFavorite`, `removeFavorite`, `checkFavoriteStatus`).
  - Add state flows and methods for Recipe Ratings (`rateRecipe`, `fetchRatingSummary`, `deleteRating`, `fetchMyRating`).

### 2. Create Expiry ViewModels
- **NEW** `ExpiryViewModel.kt`
  - Expose flows for soon-to-expire items, warning notifications, and adding/removing tracked items.
  - Store and retrieve the "Expiry Warning Days" threshold from `SessionManager`.

### 3. Session Manager Updates
- **Modify** [SessionManager.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/util/SessionManager.kt)
  - Add `saveExpiryWarningDays(days: Int)` and `getExpiryWarningDays(): Int` (default to 10).

---

## Phase 4: UI Enhancements & Screen Implementation

Create new screens, update settings controls, build a rating interface, and integrate the custom recipe creation view.

### 1. Screen Deletes / Code Cleanups
- **Modify** [UserPreferencesService.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/network/UserPreferencesService.kt): Delete endpoints `/api/reference-data/allergies` and `/api/reference-data/dislikes` (Unused & obsolete).
- Clean up unused imports of snake_case fields or old DTO dependencies across components.

### 2. Screen Additions
- **NEW** `FavoritesScreen.kt`
  - Displays a clean grid/list of recipes that have been marked as favourites.
  - Clicking any recipe opens its detailed view.
- **NEW** `ExpiryAlertsScreen.kt` or warning panel
  - Shows warning cards of products that have already expired (highlighted in red) or are expiring soon (highlighted in orange).
  - Let users configure their alert threshold directly.

### 3. Screen Modifications
- **Modify** [RecipeDetailsScreen.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/screens/RecipeDetailsScreen.kt)
  - Add a **Heart icon** in the top bar to toggle favourite status.
  - Add a **Macros Panel** under quick stats showing Protein, Carbs, Fat, and Fiber.
  - Add a **Ratings Section** under the instructions tab:
    - Display average rating (out of 5 stars) and total reviews.
    - Provide an interactive 5-star selector where users can submit or edit their rating.
    - Allow adding an optional text review and deleting the rating.
- **Modify** [SettingsScreen.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/screens/SettingsScreen.kt)
  - Add a navigation item: **"My Favourites"** (navigates to Favourites screen).
  - Add a configuration slider/selector: **"Expiry Warning Threshold"** (sets days from 1 to 30, saving to SessionManager).
- **Modify** [InventoryScreen.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/screens/InventoryScreen.kt)
  - Add an **"Add Item to Pantry" FAB**.
  - Clicking opens a dialog where users type the item name, select an expiry date using a DatePicker, and enter the quantity and unit.
  - Submitting makes a call to the Product Expiry System POST endpoint.
- **Modify** [HomeScreen.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/screens/HomeScreen.kt)
  - Display an alert banner at the top if there are items expiring soon, encouraging users to use them up.

### 4. Navigation Graph Updates
- **Modify** [Screen.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/navigation/Screen.kt)
  - Register screens: `Favorites`, `AddRecipe`, `ExpiryAlerts`.
- **Modify** [AppNavGraph.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/navigation/AppNavGraph.kt)
  - Connect navigation routes for Favourites, Expiry alerts, and Add Recipe.
- **Modify** [BottomNavigationBar.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/components/BottomNavigationBar.kt) & [BottomNavItem.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/navigation/BottomNavItem.kt)
  - Add a dedicated Favourites tab.

### 5. Add Recipe Ingredient Bug Fix (User Decided: Restrict to Database Ingredients)
- **Modify** [AddRecipeScreen.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/screens/AddRecipeScreen.kt) & [AddRecipeViewModel.kt](file:///home/sajidzaman/AndroidStudioProjects/PlanMyPlate-Android/app/src/main/java/com/teamconfused/planmyplate/ui/viewmodels/AddRecipeViewModel.kt)
  - Change the manually entered ingredient name field to an auto-complete dropdown or search dialog. Query `/api/ingredients/search` to restrict additions only to verified database ingredients, ensuring a valid `ingId` is sent on recipe creation.

---

## Phase 5: Verification & End-to-End Testing

Ensure all endpoints, serialization properties, and new UI structures are fully validated.

### 1. API Call Mock Verification
- Test JSON parsing of camelCase responses using unit tests with sample payloads.
- Ensure that the Hilt/Koin container starts and successfully resolves all dependency graphs.

### 2. Manual Verification
- Deploy backend locally, authenticate user, set preferences, check the resulting user profile and preference objects.
- Generate a weekly meal plan, mark meals as cooked, and purchase ingredients.
- Verify inventory updates, expiry alerts, and recipe ratings.
