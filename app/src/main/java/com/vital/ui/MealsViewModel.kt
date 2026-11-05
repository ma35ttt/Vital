package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.data.entity.Food
import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import com.vital.data.entity.MealLogEntry
import com.vital.data.entity.MealSlot
import com.vital.data.entity.Settings
import com.vital.data.repository.FoodRepository
import com.vital.data.repository.MealLogRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.seed.LOCAL_USER_ID
import com.vital.domain.FoodMacros
import com.vital.domain.ParseResult
import com.vital.domain.Preparation
import com.vital.domain.parseSmartText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import com.vital.domain.Food as ParserFood

/** One searchable food; a name with RAW and COOKED rows is grouped so the picker asks once. */
data class FoodChoice(
    val name: String,
    val variants: Map<FoodVariant, Food>
) {
    val default: Food
        get() = variants[FoodVariant.NONE] ?: variants[FoodVariant.RAW] ?: variants.values.first()

    val needsVariant: Boolean
        get() = FoodVariant.RAW in variants && FoodVariant.COOKED in variants
}

/**
 * Meals reads and writes the real MealLogEntry table for today. The smart-text parser is
 * fed the seeded Food rows, so logging a parsed line snapshots the same macros the search
 * rows show. Nothing here is sample data.
 */
@HiltViewModel
class MealsViewModel @Inject constructor(
    private val mealLogRepository: MealLogRepository,
    private val foodRepository: FoodRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val date: String = LocalDate.now().toString()
    private val fallback = Settings.create(LOCAL_USER_ID)

    val settings: StateFlow<Settings> = settingsRepository.getSettings(LOCAL_USER_ID)
        .map { it ?: fallback }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), fallback)

    val meals: StateFlow<List<MealLogEntry>> =
        mealLogRepository.getMealsForDate(LOCAL_USER_ID, date)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val foods: StateFlow<List<Food>> = foodRepository.getAllFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val parsed: StateFlow<ParseResult> = combine(foods, _query) { list, text ->
        if (text.isBlank()) ParseResult(emptyList(), emptyList())
        else parseSmartText(text, list.toParserFoods())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ParseResult(emptyList(), emptyList()))

    /** Search over the 600+ bundled foods, grouped by name so RAW/COOKED becomes one row. */
    val choices: StateFlow<List<FoodChoice>> = combine(foods, _query) { list, text ->
        if (text.isBlank()) emptyList()
        else list.asSequence()
            .filter { it.matches(text) }
            .groupBy { it.name.trim().lowercase() }
            .values
            .take(12)
            .map { rows -> FoodChoice(rows.first().name, rows.associateBy { it.variant }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Foods logged in the last two weeks, most recent first, to fill the sheet's Recent list. */
    val recentFoods: StateFlow<List<Food>> = combine(
        foods,
        mealLogRepository.getMealsForDateRange(
            LOCAL_USER_ID,
            LocalDate.now().minusDays(14).toString(),
            date
        )
    ) { list, logs ->
        logs.mapNotNull { it.foodId }.distinct()
            .mapNotNull { id -> list.firstOrNull { it.id == id } }
            .take(6)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Eaten kcal comes from the logged rows, not from macro-derived calories, so the hero
     *  and the slot rows always agree even when a seed's kcal does not divide by 4/4/9. */
    val eatenKcal: StateFlow<Double> = meals.map { entries ->
        entries.sumOf { it.kcal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun setQuery(text: String) {
        _query.value = text
    }

    /** Serving weight for whole items (an egg, a banana); null when only grams make sense. */
    fun gramsPerUnitFor(food: Food): Double? = food.name.gramsPerUnit()

    /** The slot the current time falls into; the ADD button logs here. */
    fun defaultSlot(): MealSlot = when (LocalTime.now().hour) {
        in 0..10 -> MealSlot.BREAKFAST
        in 11..15 -> MealSlot.LUNCH
        in 16..20 -> MealSlot.DINNER
        else -> MealSlot.SNACK
    }

    fun logFood(food: Food, grams: Double, slot: MealSlot) {
        if (grams <= 0.0) return
        val factor = grams / 100.0
        val entry = MealLogEntry.create(
            userId = LOCAL_USER_ID,
            date = date,
            mealSlot = slot,
            foodId = food.id,
            recipeId = null,
            foodName = food.name,
            variant = food.variant,
            grams = grams,
            proteinG = food.proteinPer100 * factor,
            carbsG = food.carbsPer100 * factor,
            fatG = food.fatPer100 * factor,
            kcal = food.kcalPer100 * factor,
            source = food.source
        )
        viewModelScope.launch {
            mealLogRepository.insertMealLog(entry)
            _query.value = ""
        }
    }

    /**
     * Logs every parsed item. [overrides] answers a RAW/COOKED ambiguity for one food;
     * foods without an answer use raw when the seed has both variants.
     */
    fun logParsed(slot: MealSlot, overrides: Map<String, FoodVariant> = emptyMap()) {
        val result = parsed.value
        if (result.items.isEmpty()) return
        val roomFoods = foods.value

        val entries = result.items.map { item ->
            val sameName = roomFoods.filter { it.name.equals(item.name, ignoreCase = true) }
            val variants = sameName.map { it.variant }.toSet()
            val requested = overrides[item.name] ?: when (item.preparation) {
                Preparation.RAW -> FoodVariant.RAW
                Preparation.COOKED -> FoodVariant.COOKED
                null -> when {
                    FoodVariant.RAW in variants && FoodVariant.COOKED in variants ->
                        FoodVariant.RAW
                    FoodVariant.COOKED in variants -> FoodVariant.COOKED
                    FoodVariant.RAW in variants -> FoodVariant.RAW
                    else -> FoodVariant.NONE
                }
            }
            // An explicit RAW/COOKED answer wins, but never point at a variant the seed
            // does not have; fall back to whatever the food actually stores.
            val variant = if (requested in variants || variants.isEmpty()) requested
            else variants.first()
            val room = sameName.firstOrNull { it.variant == variant } ?: sameName.firstOrNull()

            val factor = item.grams / 100.0
            MealLogEntry.create(
                userId = LOCAL_USER_ID,
                date = date,
                mealSlot = slot,
                foodId = room?.id,
                recipeId = null,
                foodName = room?.name ?: item.name,
                variant = variant,
                grams = item.grams,
                proteinG = room?.proteinPer100?.times(factor) ?: item.proteinG,
                carbsG = room?.carbsPer100?.times(factor) ?: item.carbsG,
                fatG = room?.fatPer100?.times(factor) ?: item.fatG,
                kcal = room?.kcalPer100?.times(factor) ?: item.kcal,
                source = room?.source ?: FoodSource.USER
            )
        }

        viewModelScope.launch {
            mealLogRepository.insertMealLogs(entries)
            _query.value = ""
        }
    }

    fun deleteEntry(entry: MealLogEntry) {
        viewModelScope.launch {
            mealLogRepository.softDeleteMealLog(entry.id, System.currentTimeMillis())
        }
    }
}

/**
 * Converts the seeded Food rows into the parser's grouped shape. RAW + COOKED rows become
 * one food with two macro sets, so the parser can raise its RAW/COOKED question once.
 */
private fun List<Food>.toParserFoods(): List<ParserFood> =
    groupBy { it.name.lowercase() }.map { (_, rows) ->
        val base = rows.first()
        val raw = rows.firstOrNull { it.variant == FoodVariant.RAW }
        val cooked = rows.firstOrNull { it.variant == FoodVariant.COOKED }
        val none = rows.firstOrNull { it.variant == FoodVariant.NONE }

        ParserFood(
            name = base.name,
            aliases = base.name.aliases(),
            raw = (raw ?: none)?.let { it.toParserMacros() },
            cooked = cooked?.let { it.toParserMacros() },
            gramsPerUnit = base.name.gramsPerUnit(),
            unitNames = base.name.unitNames()
        )
    }

private fun Food.toParserMacros(): FoodMacros =
    FoodMacros(
        kcalPer100g = kcalPer100,
        proteinPer100g = proteinPer100,
        carbsPer100g = carbsPer100,
        fatPer100g = fatPer100
    )

/** Name match plus the Hindi name and source code stored in [referenceName]. */
private fun Food.matches(text: String): Boolean =
    name.contains(text, ignoreCase = true) ||
        referenceName?.contains(text, ignoreCase = true) == true

/** "Eggs (Whole)" -> "eggs", "Ground Beef 93%" -> "ground beef". */
private fun String.normalizedName(): String =
    lowercase()
        .replace(Regex("\\([^)]*\\)"), "")
        .replace(Regex("\\s+\\d+%$"), "")
        .trim()

private fun String.aliases(): List<String> {
    val clean = normalizedName()
    val first = clean.split(" ").firstOrNull().orEmpty()
    val aliases = linkedSetOf(clean, first)
    if (clean.endsWith("s")) aliases += clean.dropLast(1)
    if (first.endsWith("s")) aliases += first.dropLast(1)
    // The placeholder and the brief both say "rice"; the seed calls it White Rice.
    if (clean == "white rice") aliases += "rice"
    return aliases.filter { it.isNotBlank() }
}

private fun String.gramsPerUnit(): Double? = when (normalizedName()) {
    "egg", "eggs" -> 50.0
    "egg white", "egg whites" -> 33.0
    "banana" -> 118.0
    "apple" -> 182.0
    "roti" -> 40.0
    "whole wheat bread" -> 30.0
    else -> null
}

private fun String.unitNames(): List<String> = when (normalizedName()) {
    "egg", "eggs" -> listOf("eggs", "egg")
    "egg white", "egg whites" -> listOf("egg whites", "egg white")
    "banana" -> listOf("bananas", "banana")
    "apple" -> listOf("apples", "apple")
    "roti" -> listOf("rotis", "roti")
    "whole wheat bread" -> listOf("slices", "slice")
    else -> emptyList()
}
