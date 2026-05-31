package com.teamconfused.planmyplate.domain.usecase

import com.teamconfused.planmyplate.domain.model.Recipe
import com.teamconfused.planmyplate.domain.repository.MealPlanRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DailyMeals(
    val breakfast: Recipe? = null,
    val lunch: Recipe? = null,
    val dinner: Recipe? = null
)

data class HomeMealsResult(
    val todayMeals: DailyMeals = DailyMeals(),
    val upcomingMeals: List<Recipe> = emptyList(),
    val upcomingDayLabel: String? = null,
    val upcomingMessage: String? = null,
    val hasActivePlan: Boolean = false
)

class GetTodaysMealsUseCase(
    private val mealPlanRepository: MealPlanRepository
) {
    suspend operator fun invoke(token: String, userId: Int): HomeMealsResult {
        val plans = mealPlanRepository.getWeeklyMealPlans(token, userId)
        val activePlan = plans.find { it.status.equals("active", ignoreCase = true) }

        if (activePlan == null || activePlan.slots.isNullOrEmpty()) {
            return HomeMealsResult(hasActivePlan = false)
        }

        // Logic to categorize meals
        val enrichedSlots = activePlan.slots.mapIndexed { index, slot ->
            val dayIndex = if (slot.dayNumber != null && slot.dayNumber > 0) slot.dayNumber
                           else {
                               val derived = if (slot.date != null && activePlan.startDate != null) {
                                   try {
                                       ChronoUnit.DAYS.between(
                                           LocalDate.parse(activePlan.startDate),
                                           LocalDate.parse(slot.date)
                                       ).toInt() + 1
                                   } catch (e: Exception) { 0 }
                               } else 0
                               if (derived > 0) derived else (index / 3) + 1
                           }

            val computedDate = if (activePlan.startDate != null) {
                 try {
                     LocalDate.parse(activePlan.startDate)
                         .plusDays((dayIndex - 1).toLong())
                         .toString()
                 } catch (e: Exception) { "Day $dayIndex" }
            } else {
                 slot.date ?: "Day $dayIndex"
            }
            slot to computedDate
        }

        val slotsByDate = enrichedSlots.groupBy { it.second }.toSortedMap()
        val dates = slotsByDate.keys.toList()
        val todayDateString = LocalDate.now().toString()
        
        val todayKey = if (slotsByDate.containsKey(todayDateString)) {
            todayDateString
        } else {
            dates.firstOrNull { it >= todayDateString } ?: dates.firstOrNull()
        }
        
        val todayIndex = dates.indexOf(todayKey)
        val nextDayKey = if (todayIndex != -1 && todayIndex + 1 < dates.size) {
            dates[todayIndex + 1]
        } else {
            null
        }

        val todayMealsList = if (todayKey != null) slotsByDate[todayKey]?.map { it.first } ?: emptyList() else emptyList()
        val upcomingMealsList = if (nextDayKey != null) slotsByDate[nextDayKey]?.map { it.first } ?: emptyList() else emptyList()

        val todayBreakfast = todayMealsList.find { it.mealType.equals("Breakfast", ignoreCase = true) }?.recipe
        val todayLunch = todayMealsList.find { it.mealType.equals("Lunch", ignoreCase = true) }?.recipe
        val todayDinner = todayMealsList.find { it.mealType.equals("Dinner", ignoreCase = true) }?.recipe
        
        val upcomingRecipes = upcomingMealsList.mapNotNull { it.recipe }
        
        val upcomingMsg = if (nextDayKey == null && todayKey != null) "No upcoming meals (End of Plan)" else null
        val upcomingLabel = if (nextDayKey != null) "Tomorrow" else null

        return HomeMealsResult(
            todayMeals = DailyMeals(todayBreakfast, todayLunch, todayDinner),
            upcomingMeals = upcomingRecipes,
            upcomingDayLabel = upcomingLabel,
            upcomingMessage = upcomingMsg,
            hasActivePlan = true
        )
    }
}
