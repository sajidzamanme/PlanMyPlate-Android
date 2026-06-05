# PlanMyPlate API Documentation

This document provides a detailed reference for the PlanMyPlate Python (FastAPI) API. It is designed to help developers, including Android developers, understand and integrate with the backend services.

**Base URL:** `/api`

**Interactive Docs (Swagger UI):** Available at `http://localhost:8000/docs` when the server is running.

**Authorization in Swagger:** Sign in via `/api/auth/signin`, copy the `access_token` from the response, then click the **Authorize** button at the top of Swagger UI and paste it as `Bearer <token>`.

---

## 1. Authentication

### Sign Up
Register a new user account.

- **URL:** `/api/auth/signup`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "phone": "+8801712345678",
    "dateOfBirth": "1998-05-15"
  }
  ```
  > **Validation rules:**
  > - `firstName`, `lastName`: Required, cannot be empty
  > - `password`: Minimum 8 characters
  > - `phone`: Required, 7-15 digits, optionally starting with `+`. Must be unique.
  > - `dateOfBirth`: ISO date (`YYYY-MM-DD`), must be in the past

- **Response Body:**
  ```json
  {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "token_type": "bearer",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "userId": 1,
    "phone": "+8801712345678",
    "dateOfBirth": "1998-05-15"
  }
  ```

### Sign In
Authenticate an existing user via **email or phone number**.

- **URL:** `/api/auth/signin`
- **Method:** `POST`
- **Request Body (with email):**
  ```json
  {
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }
  ```
- **Request Body (with phone):**
  ```json
  {
    "email": "+8801712345678",
    "password": "securePassword123"
  }
  ```
- **Response Body:**
  ```json
  {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "token_type": "bearer",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "userId": 1,
    "phone": "+8801712345678",
    "dateOfBirth": "1998-05-15"
  }
  ```

### Forgot Password
Initiate password reset process.

- **URL:** `/api/auth/forgot-password`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "email": "john.doe@example.com"
  }
  ```
- **Response Body:**
  ```json
  {
    "message": "Password reset token sent to email. Token: 0000"
  }
  ```

### Reset Password
Complete password reset.

- **URL:** `/api/auth/reset-password`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "resetToken": "0000",
    "newPassword": "newSecurePassword456"
  }
  ```
- **Response Body:**
  ```json
  {
    "message": "Password reset successfully"
  }
  ```

---

## 2. Users

All user endpoints require authentication (`Authorization: Bearer <token>`).

### Get Current User
Retrieve profile of the currently authenticated user.

- **URL:** `/api/users/me`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:**
  ```json
  {
    "userId": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+8801712345678",
    "dateOfBirth": "1998-05-15"
  }
  ```

### Update User
- **URL:** `/api/users/{user_id}`
- **Method:** `PUT`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "firstName": "John",
    "lastName": "Updated",
    "phone": "+8801700000000",
    "dateOfBirth": "1998-06-20",
    "age": 25,
    "weight": 70.5,
    "budget": 500.00
  }
  ```
  > All fields are optional. Only provided fields are updated.
- **Response Body:**
  ```json
  {
    "userId": 1,
    "firstName": "John",
    "lastName": "Updated",
    "email": "john.doe@example.com",
    "phone": "+8801700000000",
    "dateOfBirth": "1998-06-20"
  }
  ```


### Delete User
- **URL:** `/api/users/{user_id}`
- **Method:** `DELETE`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:**
  ```json
  {
    "message": "User deleted successfully"
  }
  ```

---

## 3. User Preferences
Manage dietary preferences, allergies, and dislikes. Requires authentication.

> Allergies and dislikes are both selected from the **ingredients list** (`GET /api/ingredients`).

### Get Preferences
- **URL:** `/api/user-preferences/{user_id}`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:**
  ```json
  {
    "prefId": 10,
    "userId": 1,
    "diets": ["Vegan"],
    "allergies": ["Peanuts", "Shellfish"],
    "dislikes": ["Mushrooms"],
    "budget": 150.00,
    "height": 170.00,
    "weight": 65.00,
    "gender": "female",
    "bmi": 22.5,
    "bmi_category": "Normal weight"
  }
  ```
  > `bmi` is computed server-side as `weight(kg) / height(m)²`. Returns `null` if height or weight is missing.  
  > `bmi_category` is age-aware: youth (<20), standard adult (20–64), senior (65+) thresholds are applied automatically.

### Set/Update Preferences
- **URL:** `/api/user-preferences/{user_id}`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "userId": 1,
    "diets": ["Vegan"],
    "allergies": ["Peanuts"],
    "dislikes": [],
    "budget": 200.00,
    "height": 170.00,
    "weight": 65.00,
    "gender": "female"
  }
  ```
  > `gender` accepted values: `"male"`, `"female"`, `"other"`.  
  > `height` in **cm**, `weight` in **kg**.  
  > `bmi` and `bmi_category` are always computed by the server — do not send them.
- **Response Body:** Updated preferences object (same structure as GET).

---

## 4. Recipes

### Get All Recipes
- **URL:** `/api/recipes`
- **Method:** `GET`
- **Query Parameters:** `skip` (default: 0), `limit` (default: 100)
- **Response Body:** List of Recipe objects.
  ```json
  [
    {
      "recipeId": 1,
      "name": "Chicken Bhuna",
      "description": "Spicy dry chicken curry",
      "calories": 520,
      "protein": 42.0,
      "carbs": 8.0,
      "fat": 18.0,
      "fiber": 1.5,
      "prepTime": 15,
      "cookTime": 35,
      "instructions": "...",
      "imageUrl": "https://...",
      "recipeIngredients": [
        {
          "id": 1,
          "ingredient": { 
            "ingId": 2, 
            "name": "Chicken", 
            "price": 4.50, 
            "tags": [
              { "tagId": 1, "tagName": "Poultry" }
            ] 
          },
          "quantity": 500,
          "unit": "g"
        }
      ]
    }
  ]
  ```
  > `protein`, `carbs`, `fat`, `fiber` are all **per serving** in grams. Any field may be `null` if not provided.
  > `tags` inside ingredients are objects with `tagId` (integer) and `tagName` (string).

### Search Recipes
- **URL:** `/api/recipes/search?name=chicken`
- **Method:** `GET`
- **Response Body:** List of matching Recipe objects.

### Filter by Calories
- **URL:** `/api/recipes/filter/calories?minCalories=200&maxCalories=500`
- **Method:** `GET`
- **Response Body:** List of Recipe objects within calorie range.

### Create Recipe
- **URL:** `/api/recipes`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "name": "Custom Pasta",
    "description": "My special pasta recipe",
    "calories": 450,
    "protein": 20.0,
    "carbs": 55.0,
    "fat": 12.0,
    "fiber": 3.0,
    "prepTime": 30,
    "cookTime": 20,
    "instructions": "1. Boil water\n2. Cook pasta\n3. Add sauce",
    "imageUrl": "https://example.com/custom_pasta.jpg",
    "ingredients": [
      {
        "ingId": 1,
        "quantity": 200,
        "unit": "g"
      }
    ]
  }
  ```
  > `protein`, `carbs`, `fat`, `fiber` are optional — all in grams per serving.
- **Response Body (201 Created):** Created Recipe object with assigned `recipeId`.

### Get Recipe by ID
- **URL:** `/api/recipes/{id}`
- **Method:** `GET`
- **Response Body:** Single Recipe object.

### Update Recipe
- **URL:** `/api/recipes/{id}`
- **Method:** `PUT`
- **Request Body:** Same format as Create Recipe.
- **Response Body:** Updated Recipe object.

### Delete Recipe
- **URL:** `/api/recipes/{id}`
- **Method:** `DELETE`
- **Response Body:**
  ```json
  {
    "message": "Recipe deleted successfully"
  }

### Cook Recipe
- **URL:** `/api/recipes/{id}/cook`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Query Parameters:**
  - `servings` (optional, float): Number of servings to cook (default: `1.0`).
  - `force` (optional, boolean): Set to `true` to bypass inventory pre-flight validation and cook anyway (default: `false`).
- **Response Body (200 OK):**
  ```json
  {
    "message": "Recipe cooked and ingredients deducted from inventory"
  }
  ```
- **Error Response (409 Conflict):**
  Returned if ingredients are missing/insufficient and `force` is `false`.
  ```json
  {
    "status": "insufficient_ingredients",
    "title": "Missing Pantry Items",
    "message": "missing inventory item",
    "missing": [
      {
        "ingId": 12,
        "name": "Chicken",
        "required": 500.0,
        "available": 200.0,
        "unit": "g"
      }
    ]
  }
  ```

---


## 5. Ingredients

### Get All Ingredients
- **URL:** `/api/ingredients`
- **Method:** `GET`
- **Response Body:** List of Ingredient objects.
  ```json
  [
    {
      "ingId": 1,
      "name": "Rice",
      "price": 1.50,
      "tags": []
    }
  ]
  ```

### Search Ingredients
- **URL:** `/api/ingredients/search?name=chicken`
- **Method:** `GET`
- **Response Body:** List of matching Ingredient objects.

### Get Ingredient by ID
- **URL:** `/api/ingredients/{id}`
- **Method:** `GET`

---

## 6. Meal Plans
All meal plan endpoints require authentication.

### Get User Meal Plans
- **URL:** `/api/meal-plans/user/{user_id}`
- **Method:** `GET`
- **Response Body:** List of MealPlan objects.

### Get Weekly Meal Plans
Retrieve active 7-day meal plans for a user.

- **URL:** `/api/meal-plans/user/{user_id}/weekly`
- **Method:** `GET`
- **Response Body:** List of MealPlan objects.
  ```json
  [
    {
      "mpId": 5,
      "userId": 1,
      "startDate": "2026-03-27",
      "duration": 7,
      "status": "active",
      "slots": [
        {
          "id": 101,
          "slotIndex": 0,
          "mealType": "Breakfast",
          "dayNumber": 1,
          "servingsMultiplier": 2,
          "recipe": { "recipeId": 1, "name": "..." }
        }
      ]
    }
  ]
  ```

### Get Meal Plans by Status
- **URL:** `/api/meal-plans/user/{user_id}/status/{status}`
- **Method:** `GET`
- **Response Body:** List of MealPlan objects matching the status.

### Get Meal Plan by ID
- **URL:** `/api/meal-plans/{id}`
- **Method:** `GET`
- **Response Body:** Single MealPlan object.

### Create Meal Plan with Recipes
Generate a meal plan with selected recipes. Also creates a grocery list with aggregated ingredients.

- **URL:** `/api/meal-plans/user/{user_id}/create`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "recipeIds": [1, 5, 12, 3, 8, 15, 2, 6, 10, 4, 7, 13, 1, 5, 12, 3, 8, 15, 2, 6, 10],
    "servingsMultipliers": [2, 1, 3, 1, 2, 1, 4, 1, 2, 1, 1, 3, 2, 1, 1, 2, 1, 3, 1, 2, 1],
    "duration": 7,
    "startDate": "2026-04-01"
  }
  ```
  > Provide up to 21 recipe IDs (3 meals × 7 days). Recipes are auto-assigned: indices 0,1,2 → Day 1 (Breakfast, Lunch, Dinner), etc.
  >
  > `servingsMultipliers` (optional): An array of integers (1–6) matching `recipeIds` length. Each value sets the serving multiplier for the corresponding recipe slot. If omitted, all default to 1.

- **Response Body (201 Created):** Created MealPlan object with populated slots.

### Update Meal Plan
- **URL:** `/api/meal-plans/{id}`
- **Method:** `PUT`
- **Request Body:**
  ```json
  {
    "status": "inactive",
    "duration": 5
  }
  ```

### Delete Meal Plan
- **URL:** `/api/meal-plans/{id}`
- **Method:** `DELETE`

### Cook Meal Slot
- **URL:** `/api/meal-plans/slots/{slot_id}/cook`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Query Parameters:**
  - `force` (optional, boolean): Set to `true` to bypass inventory pre-flight validation and cook anyway (default: `false`).
- **Response Body (200 OK):**
  ```json
  {
    "message": "Meal slot cooked and ingredients deducted from inventory"
  }
  ```
- **Error Response (409 Conflict):**
  Same validation schema as Cook Recipe.

---


## 7. Grocery Lists
All grocery list endpoints require authentication.

### Get Grocery Lists by User
- **URL:** `/api/grocery-lists/user/{user_id}`
- **Method:** `GET`
- **Response Body:** List of GroceryList objects.
  ```json
  [
    {
      "listId": 1,
      "userId": 1,
      "dateCreated": "2026-03-27",
      "status": "active",
      "items": [
        {
          "id": 501,
          "ingredient": {
            "ingId": 2,
            "name": "Chicken",
            "price": 4.50,
            "tags": []
          },
          "quantity": 500,
          "unit": "g"
        }
      ]
    }
  ]
  ```

### Get Grocery Lists by Status
- **URL:** `/api/grocery-lists/user/{user_id}/status/{status}`
- **Method:** `GET`
- **Response Body:** List of GroceryList objects matching the status.

### Get Grocery List by ID
- **URL:** `/api/grocery-lists/{id}`
- **Method:** `GET`

### Update Grocery List
- **URL:** `/api/grocery-lists/{id}`
- **Method:** `PUT`
- **Request Body:**
  ```json
  {
    "status": "completed"
  }
  ```

### Delete Grocery List
- **URL:** `/api/grocery-lists/{id}`
- **Method:** `DELETE`

### Purchase Items
Mark specific grocery list items as purchased. Purchased items are moved to the user's inventory.

- **URL:** `/api/grocery-lists/{id}/purchase`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "items": [
      { "itemId": 501, "quantity": 500 },
      { "itemId": 502, "quantity": 200 }
    ]
  }
  ```
  > Use grocery list **item IDs** (from the `id` field in grocery list items) and the **quantity** purchased.
  >
  > If the purchased quantity is less than the amount in the grocery list, the remaining quantity stays. If equal or greater, the item is removed.
  >
  > If the user doesn't have an inventory yet, one is automatically created.

- **Response Body:** `{"message": "Items purchased successfully"}`

### Update Grocery List Item
Update quantity or unit of a specific item.

- **URL:** `/api/grocery-lists/{listId}/items/{itemId}`
- **Method:** `PUT`
- **Request Body:**
  ```json
  {
    "quantity": 3,
    "unit": "Pack"
  }
  ```
- **Response Body:** Updated `GroceryListItem` object.

---

## 8. Inventory (Pantry)
All inventory endpoints require authentication.

### Get User Inventory
- **URL:** `/api/inventory/user/{user_id}`
- **Method:** `GET`
- **Response Body:**
  ```json
  {
    "invId": 1,
    "userId": 1,
    "lastUpdate": "2026-03-27",
    "items": [
      {
        "itemId": 50,
        "ingredient": { "ingId": 2, "name": "Chicken", "price": 4.50, "tags": [] },
        "quantity": 500,
        "unit": "g",
        "dateAdded": "2026-03-27",
        "expiryDate": "2026-04-03"
      }
    ]
  }
  ```
  > Returns 404 if the user doesn't have an inventory yet. An inventory is auto-created when items are purchased from a grocery list.

### Get Inventory Items
- **URL:** `/api/inventory/{inventory_id}/items`
- **Method:** `GET`
- **Response Body:** List of InvItem objects.

### Add Item to Inventory
- **URL:** `/api/inventory/{inventory_id}/items`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "ingId": 2,
    "quantity": 500,
    "unit": "g",
    "expiryDate": "2026-04-03"
  }
  ```
- **Response Body:** Created InvItem object.

### Update Inventory Item
- **URL:** `/api/inventory/items/{item_id}`
- **Method:** `PUT`
- **Request Body:**
  ```json
  {
    "quantity": 300,
    "unit": "g",
    "expiryDate": "2026-04-05"
  }
  ```
  > If quantity is set to 0 or below, the item is automatically removed.

- **Response Body:** Updated InvItem object.

### Remove Item from Inventory
- **URL:** `/api/inventory/items/{item_id}`
- **Method:** `DELETE`
- **Response Body:** `{"message": "Item removed successfully"}`

### Cook Recipe (via Inventory)
- **URL:** `/api/inventory/cook`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Query Parameters:**
  - `recipeId` (required, integer): The ID of the recipe to cook.
  - `servings` (optional, float): Number of servings to cook (default: `1.0`).
  - `force` (optional, boolean): Set to `true` to bypass inventory pre-flight validation and cook anyway (default: `false`).
- **Response Body (200 OK):**
  ```json
  {
    "message": "Recipe cooked and ingredients deducted from inventory"
  }
  ```
- **Error Response (409 Conflict):**
  Same validation schema as Cook Recipe.

---


## 9. Reference Data

### Get All Diets
- **URL:** `/api/reference-data/diets`
- **Method:** `GET`
- **Response Body:**
  ```json
  [
    { "diet_id": 1, "diet_name": "Omnivore" },
    { "diet_id": 2, "diet_name": "Vegetarian" },
    { "diet_id": 3, "diet_name": "Vegan" }
  ]
  ```


> For **allergies and dislikes**, use `GET /api/ingredients` — users select from the same ingredient list for both.

---

## 10. File Uploads

### Upload Image
Upload an image file to the server and get a URL to use in recipes.

> The `imageUrl` field in Recipe endpoints accepts either an external online URL (e.g., Unsplash) or the internal URL returned by this upload endpoint.

- **URL:** `/api/files/upload`
- **Method:** `POST`
- **Content-Type:** `multipart/form-data`
- **Request Parameters:**
    - `file`: The image file
- **Response Body:**
  ```json
  {
    "url": "http://localhost:8000/uploads/uuid-filename.jpg",
    "filename": "uuid-filename.jpg"
  }
  ```

---

## 11. AI Recipe Generation
Requires authentication and a configured Gemini API key.

### Generate Recipe with AI
Generate a custom recipe using Google Gemini AI based on user preferences and constraints.

- **URL:** `/api/ai/generate-recipe`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "availableIngredients": ["chicken", "tomatoes", "garlic", "pasta"],
    "maxCalories": 600,
    "cuisineType": "Italian",
    "allergies": ["peanuts"],
    "dietaryPreference": "None",
    "mood": "Comfort Food",
    "maxCookingTime": 45
  }
  ```

**Request Parameters:**
- `availableIngredients` (optional): List of ingredients you have available
- `maxCalories` (optional): Maximum calories per serving (50-5000)
- `cuisineType` (optional): Desired cuisine (e.g., Italian, Indian, Mexican)
- `allergies` (optional): List of allergens to avoid
- `dietaryPreference` (optional): Dietary restriction (e.g., Vegan, Vegetarian, Keto)
- `mood` (optional): Occasion or mood (e.g., Comfort Food, Quick & Easy)
- `maxCookingTime` (optional): Maximum total cooking time in minutes (5-300)

- **Response Body (201 Created):** Created Recipe object (same structure as Recipe endpoints).

> - The AI will create new ingredients in the database if they don't already exist
> - Generated recipes are automatically saved to the database
> - Set your Gemini API key in `.env`: `GEMINI_API_KEY=YOUR_KEY`

### Generate Weekly Meal Plan with AI
Generate a complete weekly meal plan (21 meals) based on user preferences.

- **URL:** `/api/ai/generate-meal-plan`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Query Parameters:**
    - `userId` (required): ID of the user
    - `startDate` (optional): Start date in YYYY-MM-DD format (defaults to today)
- **Response Body (201 Created):** Created MealPlan object with 21 slots.

---

## 12. Product Expiry System
Track product expiry dates for items in the user's pantry/inventory.  
All endpoints require `Authorization: Bearer <token>`.

> **How it works:** When a user buys a product they enter the product name and expiry date. The product name is matched **case-insensitively** against existing ingredients — a new ingredient is created automatically if no match is found. Items are stored directly in the user's inventory (`inv_item` table). An inventory is auto-created if the user doesn't have one yet.

> **Scheduled use:** The mobile app reads the user's *"expiry warning days"* setting and passes it as the `days` query parameter on a scheduled call to `/soon`. The server default is **10 days**.

---

### Add a Product with Expiry Date
Record a purchased product and its expiry date.

- **URL:** `/api/expiry/user/{user_id}/items`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "productName": "Milk",
    "expiryDate": "2026-05-18",
    "quantity": 2,
    "unit": "litre"
  }
  ```
  > **Validation rules:**
  > - `productName`: Required, 1–150 characters
  > - `expiryDate`: Required, ISO date (`YYYY-MM-DD`). Past dates are **allowed** (item may already be expired)
  > - `quantity`: Must be > 0, defaults to `1.0`
  > - `unit`: Defaults to `"unit"`

- **Response Body (201 Created):**
  ```json
  {
    "itemId": 42,
    "productName": "Milk",
    "expiryDate": "2026-05-18",
    "dateAdded": "2026-05-08",
    "quantity": 2.0,
    "unit": "litre",
    "daysUntilExpiry": 10,
    "isExpired": false
  }
  ```
  > `daysUntilExpiry` — days remaining until expiry (negative means already expired).  
  > `isExpired` — `true` when `daysUntilExpiry < 0`.

---

### List All Expiry-Tracked Items
Returns all inventory items that have an expiry date, ordered soonest-first.

- **URL:** `/api/expiry/user/{user_id}/items`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:** List of expiry item objects (same structure as above).

---

### Get Soon-to-Expire Items *(Scheduled endpoint)*
Returns items expiring within the next N days. Already-expired items are **included** and flagged.

- **URL:** `/api/expiry/user/{user_id}/soon`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Query Parameters:**
  | Parameter | Type | Default | Description |
  |-----------|------|---------|-------------|
  | `days` | integer | `10` | Warning threshold in days. Range: `0–3650`. Pass the user's setting value from the app. |

- **Response Body:**
  ```json
  {
    "thresholdDays": 10,
    "totalCount": 3,
    "expiredCount": 1,
    "items": [
      {
        "itemId": 41,
        "productName": "Yogurt",
        "expiryDate": "2026-05-05",
        "dateAdded": "2026-04-28",
        "quantity": 1.0,
        "unit": "unit",
        "daysUntilExpiry": -3,
        "isExpired": true
      },
      {
        "itemId": 42,
        "productName": "Milk",
        "expiryDate": "2026-05-18",
        "dateAdded": "2026-05-08",
        "quantity": 2.0,
        "unit": "litre",
        "daysUntilExpiry": 10,
        "isExpired": false
      }
    ]
  }
  ```
  > `thresholdDays` — echoed back so the client can confirm what threshold was applied.  
  > `expiredCount` — count of items whose expiry date is strictly before today.  
  > `days=0` returns only items expiring exactly today plus already-expired items.

---

### Update an Expiry Item
Partially update the expiry date, quantity, or unit of a tracked product. Only supplied fields are changed.

- **URL:** `/api/expiry/items/{item_id}`
- **Method:** `PUT`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body (all fields optional):**
  ```json
  {
    "expiryDate": "2026-05-22",
    "quantity": 1.5,
    "unit": "litre"
  }
  ```
- **Response Body:** Updated expiry item object.

---

### Delete an Expiry Item
Permanently removes an item from the inventory.

- **URL:** `/api/expiry/items/{item_id}`
- **Method:** `DELETE`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:**
  ```json
  {
    "message": "Item removed successfully"
  }
  ```

---

### Expiry System — Corner Cases

| Scenario | Behaviour |
|----------|-----------|
| Product name not in DB | New ingredient created automatically (price = 0) |
| User has no inventory | Inventory auto-created on first POST |
| Past expiry date on POST | Allowed — item added with `isExpired: true` |
| Same product, different batch | New row always inserted; independent expiry dates tracked |
| `days=0` on `/soon` | Returns today's expiring + already-expired only |
| `days` outside `0–3650` | HTTP 422 validation error |
| Empty `productName` | HTTP 422 validation error |
| Wrong user token | HTTP 400 — Not enough permissions |
| Item not found | HTTP 404 |
| Item belongs to another user | HTTP 400 — Not enough permissions |
| User has no expiry items | Returns empty list with `totalCount: 0` (not 404) |

---

## 13. Recipe Ratings
Rate recipes and retrieve rating summaries. All write endpoints require authentication.

### Rate a Recipe (Create or Update)
Submit a 1–5 star rating with an optional text review. If the user has already rated this recipe, the existing rating is updated (upsert).

- **URL:** `/api/ratings/`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "recipeId": 1,
    "rating": 4,
    "review": "Really flavourful, would make again!"
  }
  ```
  > `rating`: Integer from **1 to 5** (required).  
  > `review`: Optional free-text string.
- **Response Body (201 Created):**
  ```json
  {
    "ratingId": 7,
    "userId": 1,
    "recipeId": 1,
    "rating": 4,
    "review": "Really flavourful, would make again!",
    "createdAt": "2026-05-15T10:30:00",
    "updatedAt": "2026-05-15T10:30:00"
  }
  ```

### Get My Rating for a Recipe
Retrieve the current user's own rating for a specific recipe.

- **URL:** `/api/ratings/my/{recipe_id}`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:** `RecipeRatingResponse` object (same structure as above). Returns **404** if the user hasn't rated this recipe yet.

### Get Rating Summary for a Recipe
Get the aggregated average rating and total count for any recipe. No authentication required.

- **URL:** `/api/ratings/recipe/{recipe_id}`
- **Method:** `GET`
- **Response Body:**
  ```json
  {
    "recipeId": 1,
    "averageRating": 4.3,
    "totalRatings": 12
  }
  ```
  > Returns **404** if the recipe does not exist.

### Delete My Rating
Remove the current user's rating for a recipe.

- **URL:** `/api/ratings/{recipe_id}`
- **Method:** `DELETE`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:**
  ```json
  { "message": "Rating deleted successfully" }
  ```
  > Returns **404** if no rating exists for this user/recipe pair.

---

## 14. Favourites
Save and manage favourite recipes. All endpoints require authentication.

### Add a Recipe to Favourites
- **URL:** `/api/favorites/{recipe_id}`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body (201 Created):**
  ```json
  {
    "id": 3,
    "userId": 1,
    "recipeId": 5,
    "recipe": {
      "recipeId": 5,
      "name": "Rui Machher Jhol",
      "description": "Light fish curry",
      "calories": 450,
      "protein": null,
      "carbs": null,
      "fat": null,
      "fiber": null,
      "prepTime": 15,
      "cookTime": 30,
      "imageUrl": "https://...",
      "recipeIngredients": []
    },
    "createdAt": "2026-05-15T10:45:00"
  }
  ```
  > Returns **404** if recipe does not exist. Returns **409** if the recipe is already in favourites.

### Remove a Recipe from Favourites
- **URL:** `/api/favorites/{recipe_id}`
- **Method:** `DELETE`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:**
  ```json
  { "message": "Favorite removed successfully" }
  ```
  > Returns **404** if the recipe was not in favourites.

### List My Favourite Recipes
- **URL:** `/api/favorites/`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Query Parameters:** `skip` (default: 0), `limit` (default: 100)
- **Response Body:** List of `UserFavoriteResponse` objects (same structure as Add response above).

### Check Favourite Status
Quickly check whether a specific recipe is in the current user's favourites.

- **URL:** `/api/favorites/{recipe_id}/status`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:**
  ```json
  { "isFavorite": true }
  ```

---

## 15. Admin Endpoints

Admin endpoints for managing reference data and performing administrative tasks. Require authentication (`Authorization: Bearer <token>`).

> **Note:** These endpoints are grouped under the "admin" tag in Swagger UI and use the `/api/admin` prefix. Currently any authenticated user can access them; role-based access control will be added in the future.

### Get User by ID
Look up any user's profile.

- **URL:** `/api/admin/users/{user_id}`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer <token>`
- **Response Body:** Same structure as Get Current User.

### Create Ingredient
- **URL:** `/api/admin/ingredients`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "name": "Coconut",
    "price": 3.50
  }
  ```
- **Response Body (201 Created):**
  ```json
  {
    "ingId": 12,
    "name": "Coconut",
    "price": 3.50,
    "tags": []
  }
  ```

### Update Ingredient
- **URL:** `/api/admin/ingredients/{id}`
- **Method:** `PUT`
- **Request Body:**
  ```json
  {
    "name": "Updated Name",
    "price": 4.00
  }
  ```
- **Response Body:**
  ```json
  {
    "ingId": 12,
    "name": "Updated Name",
    "price": 4.00,
    "tags": []
  }
  ```

### Delete Ingredient
- **URL:** `/api/admin/ingredients/{id}`
- **Method:** `DELETE`
- **Response Body:**
  ```json
  {
    "message": "Ingredient deleted successfully"
  }
  ```

### Update Recipe
- **URL:** `/api/admin/recipes/{id}`
- **Method:** `PUT`
- **Request Body:** Same format as Create Recipe.
- **Response Body:** Updated Recipe object.

### Delete Recipe
- **URL:** `/api/admin/recipes/{id}`
- **Method:** `DELETE`
- **Response Body:**
  ```json
  {
    "message": "Recipe deleted successfully"
  }
  ```

---

## 16. Error Handling

The API uses standard HTTP status codes and returns structured, user-friendly error responses. All exception detail messages are written in plain, clear language so that the frontend can directly display the `"detail"` string (e.g. in dialogs or toast messages) without any extra translation.

### Error Response Structure
```json
{
  "detail": "Recipe not found"
}
```


### Common Status Codes
| Status Code | Description | Scenario |
|-------------|-------------|----------|
| `200 OK` | Success | Request completed successfully. |
| `201 Created` | Created | Resource was successfully created. |
| `400 Bad Request` | Client Error | Missing required fields, invalid format, or not enough permissions. |
| `403 Forbidden` | Auth Error | Could not validate credentials (invalid JWT token). |
| `404 Not Found` | Not Found | Requested resource does not exist. |
| `409 Conflict` | Conflict | Resource already exists (e.g., duplicate inventory). |
| `422 Unprocessable Entity` | Validation Error | Request body fails Pydantic validation. |
| `500 Server Error` | Internal Error | Something went wrong on the server. |

### Validation Error Response
FastAPI returns detailed validation errors:
```json
{
  "detail": [
    {
      "loc": ["body", "email"],
      "msg": "value is not a valid email address",
      "type": "value_error.email"
    }
  ]
}
```

---
