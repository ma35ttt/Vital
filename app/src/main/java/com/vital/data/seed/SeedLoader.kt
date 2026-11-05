package com.vital.data.seed

import android.content.Context
import com.vital.data.entity.BodyMeasurements
import com.vital.data.entity.Food
import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import com.vital.data.entity.Recipe
import com.vital.data.entity.Settings
import com.vital.data.entity.User
import com.vital.data.repository.ExerciseRepository
import com.vital.data.repository.FoodRepository
import com.vital.data.repository.RecipeRepository
import com.vital.data.repository.SettingsRepository
import com.vital.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString

/** The only user row until the M6 auth gate creates real ones. */
const val LOCAL_USER_ID = "local-user"

/** Bundled, comprehensive exercise catalog (curated + public-domain free-exercise-db). */
private const val EXERCISE_CATALOG_ASSET = "exercises.json"

/** Bundled Indian food composition table (IFCT 2017 + Gopalan 2004, CC0-1.0). */
private const val FOOD_CATALOG_ASSET = "foods.json"

@Singleton
class SeedLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foodRepository: FoodRepository,
    private val recipeRepository: RecipeRepository,
    private val exerciseRepository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) {

    /**
     * Writes the Settings and User rows on first launch so the Settings screen reads
     * real persisted values instead of a hardcoded default. Safe to call every launch.
     */
    suspend fun seedUserAndSettings() = withContext(Dispatchers.IO) {
        if (userRepository.getUserSync(LOCAL_USER_ID) == null) {
            userRepository.insertUser(
                User(id = LOCAL_USER_ID, name = "Mrityunjay", email = "")
            )
        }
        if (settingsRepository.getSettings(LOCAL_USER_ID).first() == null) {
            settingsRepository.insertSettings(
                Settings.create(LOCAL_USER_ID).copy(bodyWeightKg = 80.0)
            )
        }
    }

    suspend fun loadIfNeeded() = withContext(Dispatchers.IO) {
        val foodCount = foodRepository.getFoodCount()
        if (foodCount == 0) {
            loadFoods()
            loadRecipes()
        }
        // Idempotent by name, so an upgrade adds only the exercises the DB does not have yet.
        loadExercises()
        // Same for the Indian food library: existing installs gain the new foods on upgrade.
        loadFoodCatalog()
    }

    private suspend fun loadFoods() {
        // 28 distinct foods, 32 rows (4 have RAW/COOKED variants)
        val foods = listOf(
            // Core 22 (4 with RAW/COOKED = chicken breast, white rice, pasta, toor dal = 4*2 + 18 = 26, wait...)
            // Actually: chicken breast (raw, cooked), white rice (raw, cooked), pasta (raw, cooked), toor dal (raw, cooked) = 4*2 = 8
            // + 18 others = 26... let me count properly
            // Core: chicken breast (raw, cooked), eggs, egg whites, paneer, curd, milk, whey, white rice (raw, cooked), roti, oats, pasta (raw, cooked), potato, banana, apple, peanuts, almonds, olive oil, ghee, toor dal (raw, cooked), chickpeas, broccoli, spinach = 22 items, 4 with variants = 26 rows
            // Extra 6: tomato, onion, tofu, soya chunks, peanut butter, whole wheat bread = 6
            // Total: 28 distinct, 32 rows

            // CHICKEN BREAST - RAW
            Food.create("Chicken Breast", FoodVariant.RAW, 23.1, 0.0, 1.5, FoodSource.SEED, referenceName = "USDA Chicken, broilers or fryers, breast, meat only, raw"),
            // CHICKEN BREAST - COOKED
            Food.create("Chicken Breast", FoodVariant.COOKED, 31.0, 0.0, 3.6, FoodSource.SEED, referenceName = "USDA Chicken, broilers or fryers, breast, meat only, cooked, grilled"),

            // EGGS
            Food.create("Eggs (Whole)", FoodVariant.NONE, 12.6, 1.1, 9.5, FoodSource.SEED, referenceName = "USDA Egg, whole, cooked, hard-boiled"),
            Food.create("Egg Whites", FoodVariant.NONE, 10.9, 0.7, 0.2, FoodSource.SEED, referenceName = "USDA Egg, white, cooked"),

            // DAIRY
            Food.create("Paneer", FoodVariant.NONE, 18.3, 1.2, 20.8, FoodSource.SEED, referenceName = "IFCT Paneer (cottage cheese)"),
            Food.create("Curd (Plain)", FoodVariant.NONE, 3.5, 3.0, 3.3, FoodSource.SEED, referenceName = "USDA Yogurt, plain, whole milk"),
            Food.create("Milk (Toned)", FoodVariant.NONE, 3.1, 4.7, 3.0, FoodSource.SEED, referenceName = "IFCT Milk, cow, toned"),
            Food.create("Whey Protein", FoodVariant.NONE, 80.0, 5.0, 6.0, FoodSource.SEED, referenceName = "USDA Protein powder, whey based"),

            // GRAINS - WHITE RICE RAW
            Food.create("White Rice", FoodVariant.RAW, 7.1, 79.8, 0.7, FoodSource.SEED, referenceName = "USDA Rice, white, long-grain, regular, raw"),
            // WHITE RICE COOKED
            Food.create("White Rice", FoodVariant.COOKED, 2.7, 28.0, 0.3, FoodSource.SEED, referenceName = "USDA Rice, white, long-grain, regular, cooked"),

            Food.create("Roti (Wheat)", FoodVariant.NONE, 7.9, 46.1, 1.7, FoodSource.SEED, referenceName = "IFCT Chapati (whole wheat)"),
            Food.create("Oats (Raw)", FoodVariant.RAW, 16.9, 66.3, 6.9, FoodSource.SEED, referenceName = "USDA Oats, regular, dry"),

            // PASTA RAW
            Food.create("Pasta", FoodVariant.RAW, 13.0, 75.0, 1.5, FoodSource.SEED, referenceName = "USDA Pasta, dry, enriched"),
            // PASTA COOKED
            Food.create("Pasta", FoodVariant.COOKED, 5.8, 30.9, 0.9, FoodSource.SEED, referenceName = "USDA Pasta, cooked, enriched, without added salt"),

            Food.create("Potato (Boiled)", FoodVariant.NONE, 1.9, 17.5, 0.1, FoodSource.SEED, referenceName = "USDA Potato, flesh and skin, boiled"),

            // FRUITS
            Food.create("Banana", FoodVariant.NONE, 1.1, 22.8, 0.3, FoodSource.SEED, referenceName = "USDA Bananas, raw"),
            Food.create("Apple", FoodVariant.NONE, 0.3, 13.8, 0.2, FoodSource.SEED, referenceName = "USDA Apples, raw, with skin"),

            // NUTS & OILS
            Food.create("Peanuts", FoodVariant.NONE, 25.8, 16.1, 49.2, FoodSource.SEED, referenceName = "USDA Peanuts, all types, dry-roasted, without salt"),
            Food.create("Almonds", FoodVariant.NONE, 21.2, 21.6, 49.9, FoodSource.SEED, referenceName = "USDA Almonds, dry roasted, without salt"),
            Food.create("Olive Oil", FoodVariant.NONE, 0.0, 0.0, 100.0, FoodSource.SEED, referenceName = "USDA Oil, olive, salad or cooking"),
            Food.create("Ghee", FoodVariant.NONE, 0.0, 0.0, 99.8, FoodSource.SEED, referenceName = "IFCT Ghee (clarified butter)"),

            // LEGUMES - TOOR DAL RAW
            Food.create("Toor Dal", FoodVariant.RAW, 22.3, 57.6, 1.7, FoodSource.SEED, referenceName = "IFCT Arhar dal (pigeon pea), raw"),
            // TOOR DAL COOKED
            Food.create("Toor Dal", FoodVariant.COOKED, 7.0, 18.5, 0.5, FoodSource.SEED, referenceName = "IFCT Arhar dal, cooked"),

            Food.create("Chickpeas (Cooked)", FoodVariant.NONE, 8.9, 27.4, 2.6, FoodSource.SEED, referenceName = "USDA Chickpeas (garbanzo beans), mature seeds, cooked, boiled"),

            // VEGETABLES
            Food.create("Broccoli", FoodVariant.NONE, 2.8, 6.6, 0.4, FoodSource.SEED, referenceName = "USDA Broccoli, raw"),
            Food.create("Spinach", FoodVariant.NONE, 2.9, 3.6, 0.4, FoodSource.SEED, referenceName = "USDA Spinach, raw"),

            // EXTRA 6
            Food.create("Tomato", FoodVariant.NONE, 0.9, 3.9, 0.2, FoodSource.SEED, referenceName = "USDA Tomatoes, red, raw"),
            Food.create("Onion", FoodVariant.NONE, 1.1, 9.3, 0.1, FoodSource.SEED, referenceName = "USDA Onions, raw"),
            Food.create("Tofu", FoodVariant.NONE, 8.1, 1.9, 4.8, FoodSource.SEED, referenceName = "USDA Tofu, firm, prepared with calcium sulfate"),
            Food.create("Soya Chunks", FoodVariant.RAW, 52.0, 33.0, 0.5, FoodSource.SEED, referenceName = "IFCT Soya nuggets/chunks"),
            Food.create("Peanut Butter", FoodVariant.NONE, 25.0, 20.0, 50.0, FoodSource.SEED, referenceName = "USDA Peanut butter, smooth style, without salt"),
            Food.create("Whole Wheat Bread", FoodVariant.NONE, 12.3, 41.3, 3.3, FoodSource.SEED, referenceName = "USDA Bread, whole-wheat, commercially prepared")
        )
        foodRepository.insertFoods(foods)
    }

    private suspend fun loadRecipes() {
        // Chicken tomato pasta
        val chickenTomatoIngredients = listOf(
            com.vital.data.entity.RecipeIngredient("chicken_breast_raw_id", "Chicken Breast (Raw)", 200.0, FoodVariant.RAW),
            com.vital.data.entity.RecipeIngredient("pasta_raw_id", "Pasta (Raw)", 100.0, FoodVariant.RAW),
            com.vital.data.entity.RecipeIngredient("tomato_id", "Tomato", 150.0, FoodVariant.NONE),
            com.vital.data.entity.RecipeIngredient("onion_id", "Onion", 50.0, FoodVariant.NONE),
            com.vital.data.entity.RecipeIngredient("olive_oil_id", "Olive Oil", 10.0, FoodVariant.NONE)
        )
        val chickenTomatoJson = Json.encodeToString(chickenTomatoIngredients)
        val chickenTomato = Recipe.create("Chicken Tomato Pasta", 450.0, chickenTomatoJson, "Serves 2")

        // Paneer gravy pasta
        val paneerGravyIngredients = listOf(
            com.vital.data.entity.RecipeIngredient("paneer_id", "Paneer", 200.0, FoodVariant.NONE),
            com.vital.data.entity.RecipeIngredient("pasta_raw_id", "Pasta (Raw)", 100.0, FoodVariant.RAW),
            com.vital.data.entity.RecipeIngredient("tomato_id", "Tomato", 150.0, FoodVariant.NONE),
            com.vital.data.entity.RecipeIngredient("onion_id", "Onion", 50.0, FoodVariant.NONE),
            com.vital.data.entity.RecipeIngredient("ghee_id", "Ghee", 15.0, FoodVariant.NONE)
        )
        val paneerGravyJson = Json.encodeToString(paneerGravyIngredients)
        val paneerGravy = Recipe.create("Paneer Gravy Pasta", 450.0, paneerGravyJson, "Serves 2")

        recipeRepository.insertRecipes(listOf(chickenTomato, paneerGravy))
    }

    private suspend fun loadExercises() {
        val catalogJson = context.assets
            .open(EXERCISE_CATALOG_ASSET)
            .bufferedReader()
            .use { it.readText() }
        val catalog = ExerciseCatalog.parse(catalogJson)

        val existing = exerciseRepository.getAllExercises().first()
            .map { it.name.trim().lowercase() }
            .toSet()
        val missing = catalog.filter { it.name.trim().lowercase() !in existing }
        if (missing.isNotEmpty()) {
            exerciseRepository.insertExercises(missing)
        }
    }

    /**
     * Adds any bundled Indian food that is not stored yet. Matching is on (name, variant) so
     * the curated RAW/COOKED rows keep winning and a re-launch never duplicates the library.
     */
    private suspend fun loadFoodCatalog() {
        val catalogJson = context.assets
            .open(FOOD_CATALOG_ASSET)
            .bufferedReader()
            .use { it.readText() }
        val catalog = FoodCatalog.parse(catalogJson)

        val existing = foodRepository.getAllFoods().first()
            .map { it.name.trim().lowercase() to it.variant }
            .toSet()
        val missing = catalog.filter { (it.name.trim().lowercase() to it.variant) !in existing }
        if (missing.isNotEmpty()) {
            foodRepository.insertFoods(missing)
        }
    }
}
