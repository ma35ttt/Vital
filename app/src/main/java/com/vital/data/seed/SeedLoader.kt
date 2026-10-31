package com.vital.data.seed

import android.content.Context
import com.vital.data.entity.BodyMeasurements
import com.vital.data.entity.Exercise
import com.vital.data.entity.Food
import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import com.vital.data.entity.MuscleGroup
import com.vital.data.entity.Recipe
import com.vital.data.repository.ExerciseRepository
import com.vital.data.repository.FoodRepository
import com.vital.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString

@Singleton
class SeedLoader @Inject constructor(
    private val foodRepository: FoodRepository,
    private val recipeRepository: RecipeRepository,
    private val exerciseRepository: ExerciseRepository
) {

    suspend fun loadIfNeeded() = withContext(Dispatchers.IO) {
        val foodCount = foodRepository.getFoodCount()
        if (foodCount == 0) {
            loadFoods()
            loadRecipes()
        }
        val exerciseCount = exerciseRepository.getExerciseCount()
        if (exerciseCount == 0) {
            loadExercises()
        }
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
        // Combined curated list from fud-ai-android + openGym (~200 exercises)
        // For now, adding a core set of exercises with defaultRestSec and isCompound
        val exercises = listOf(
            // CHEST
            Exercise.create("Bench Press", MuscleGroup.CHEST, true, com.vital.data.entity.Equipment.BARBELL, "Lie on bench, lower bar to chest, press up", 180, true),
            Exercise.create("Incline Bench Press", MuscleGroup.CHEST, true, com.vital.data.entity.Equipment.BARBELL, "Incline bench, lower to upper chest", 180, true),
            Exercise.create("Dumbbell Bench Press", MuscleGroup.CHEST, false, com.vital.data.entity.Equipment.DUMBBELL, "Press dumbbells from chest level", 120, true),
            Exercise.create("Incline Dumbbell Press", MuscleGroup.CHEST, false, com.vital.data.entity.Equipment.DUMBBELL, "Incline bench with dumbbells", 120, true),
            Exercise.create("Dumbbell Flyes", MuscleGroup.CHEST, false, com.vital.data.entity.Equipment.DUMBBELL, "Arms wide, squeeze chest at top", 90, false),
            Exercise.create("Cable Crossovers", MuscleGroup.CHEST, false, com.vital.data.entity.Equipment.CABLE, "Cross cables at midline", 90, false),
            Exercise.create("Push-ups", MuscleGroup.CHEST, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Body straight, lower chest to floor", 60, true),
            Exercise.create("Dips (Chest)", MuscleGroup.CHEST, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Lean forward, lower until shoulders below elbows", 90, true),

            // BACK
            Exercise.create("Deadlift", MuscleGroup.BACK, true, com.vital.data.entity.Equipment.BARBELL, "Hinge at hips, pull bar from floor", 180, true),
            Exercise.create("Pull-ups", MuscleGroup.BACK, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Overhand grip, pull chin over bar", 120, true),
            Exercise.create("Chin-ups", MuscleGroup.BACK, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Underhand grip, pull chin over bar", 120, true),
            Exercise.create("Barbell Row", MuscleGroup.BACK, true, com.vital.data.entity.Equipment.BARBELL, "Bent over, pull bar to lower chest", 120, true),
            Exercise.create("Dumbbell Row", MuscleGroup.BACK, false, com.vital.data.entity.Equipment.DUMBBELL, "One arm, pull to hip", 90, true),
            Exercise.create("Lat Pulldown", MuscleGroup.BACK, false, com.vital.data.entity.Equipment.MACHINE, "Wide grip, pull to upper chest", 90, true),
            Exercise.create("Seated Cable Row", MuscleGroup.BACK, false, com.vital.data.entity.Equipment.CABLE, "Pull to abdomen, squeeze shoulder blades", 90, true),
            Exercise.create("T-Bar Row", MuscleGroup.BACK, true, com.vital.data.entity.Equipment.BARBELL, "Chest supported, pull to chest", 120, true),
            Exercise.create("Face Pulls", MuscleGroup.BACK, false, com.vital.data.entity.Equipment.CABLE, "High pulley, pull to face", 60, false),

            // SHOULDERS
            Exercise.create("Overhead Press", MuscleGroup.SHOULDERS, true, com.vital.data.entity.Equipment.BARBELL, "Press bar from shoulders overhead", 120, true),
            Exercise.create("Dumbbell Shoulder Press", MuscleGroup.SHOULDERS, false, com.vital.data.entity.Equipment.DUMBBELL, "Press dumbbells overhead", 90, true),
            Exercise.create("Lateral Raises", MuscleGroup.SHOULDERS, false, com.vital.data.entity.Equipment.DUMBBELL, "Raise arms to shoulder height", 60, false),
            Exercise.create("Front Raises", MuscleGroup.SHOULDERS, false, com.vital.data.entity.Equipment.DUMBBELL, "Raise arms forward to shoulder height", 60, false),
            Exercise.create("Rear Delt Flyes", MuscleGroup.SHOULDERS, false, com.vital.data.entity.Equipment.DUMBBELL, "Bent over, raise arms to side", 60, false),
            Exercise.create("Arnold Press", MuscleGroup.SHOULDERS, false, com.vital.data.entity.Equipment.DUMBBELL, "Rotate palms during press", 90, true),
            Exercise.create("Upright Rows", MuscleGroup.SHOULDERS, true, com.vital.data.entity.Equipment.BARBELL, "Pull bar to chin, elbows high", 90, true),
            Exercise.create("Pike Push-ups", MuscleGroup.SHOULDERS, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Hips high, lower head to floor", 60, true),

            // ARMS - BICEPS
            Exercise.create("Barbell Curl", MuscleGroup.ARMS, true, com.vital.data.entity.Equipment.BARBELL, "Curl bar to shoulders", 90, false),
            Exercise.create("Dumbbell Curl", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.DUMBBELL, "Supinate at top", 60, false),
            Exercise.create("Hammer Curl", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.DUMBBELL, "Neutral grip throughout", 60, false),
            Exercise.create("Preacher Curl", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.MACHINE, "Isolate biceps", 60, false),
            Exercise.create("Cable Curl", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.CABLE, "Constant tension", 60, false),

            // ARMS - TRICEPS
            Exercise.create("Close Grip Bench Press", MuscleGroup.ARMS, true, com.vital.data.entity.Equipment.BARBELL, "Hands shoulder width", 120, true),
            Exercise.create("Tricep Dips", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Upright torso", 90, true),
            Exercise.create("Overhead Tricep Extension", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.DUMBBELL, "Lower behind head", 60, false),
            Exercise.create("Rope Pushdowns", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.CABLE, "Spread rope at bottom", 60, false),
            Exercise.create("Skull Crushers", MuscleGroup.ARMS, false, com.vital.data.entity.Equipment.BARBELL, "Lower to forehead", 90, false),

            // LEGS
            Exercise.create("Squat", MuscleGroup.LEGS, true, com.vital.data.entity.Equipment.BARBELL, "Hip crease below knee", 180, true),
            Exercise.create("Front Squat", MuscleGroup.LEGS, true, com.vital.data.entity.Equipment.BARBELL, "Bar on front delts", 180, true),
            Exercise.create("Leg Press", MuscleGroup.LEGS, false, com.vital.data.entity.Equipment.MACHINE, "Full range of motion", 120, true),
            Exercise.create("Romanian Deadlift", MuscleGroup.LEGS, true, com.vital.data.entity.Equipment.BARBELL, "Hinge at hips, slight knee bend", 120, true),
            Exercise.create("Lunges", MuscleGroup.LEGS, false, com.vital.data.entity.Equipment.DUMBBELL, "Alternating or walking", 90, true),
            Exercise.create("Bulgarian Split Squat", MuscleGroup.LEGS, false, com.vital.data.entity.Equipment.DUMBBELL, "Rear foot elevated", 90, true),
            Exercise.create("Leg Extension", MuscleGroup.LEGS, false, com.vital.data.entity.Equipment.MACHINE, "Full extension", 60, false),
            Exercise.create("Leg Curl", MuscleGroup.LEGS, false, com.vital.data.entity.Equipment.MACHINE, "Lying or seated", 60, false),
            Exercise.create("Calf Raises", MuscleGroup.LEGS, false, com.vital.data.entity.Equipment.MACHINE, "Full stretch at bottom", 60, false),
            Exercise.create("Hip Thrust", MuscleGroup.LEGS, true, com.vital.data.entity.Equipment.BARBELL, "Upper back on bench", 120, true),

            // CORE
            Exercise.create("Plank", MuscleGroup.CORE, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Hold straight line", 60, false),
            Exercise.create("Hanging Leg Raises", MuscleGroup.CORE, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Raise legs to parallel", 90, false),
            Exercise.create("Ab Wheel Rollout", MuscleGroup.CORE, false, com.vital.data.entity.Equipment.OTHER, "Full extension", 90, false),
            Exercise.create("Russian Twists", MuscleGroup.CORE, false, com.vital.data.entity.Equipment.DUMBBELL, "Rotate torso", 60, false),
            Exercise.create("Cable Crunch", MuscleGroup.CORE, false, com.vital.data.entity.Equipment.CABLE, "Kneeling, crunch down", 60, false),

            // CARDIO
            Exercise.create("Running", MuscleGroup.CARDIO, false, com.vital.data.entity.Equipment.NONE, "Outdoor or treadmill", 0, false),
            Exercise.create("Cycling", MuscleGroup.CARDIO, false, com.vital.data.entity.Equipment.OTHER, "Stationary or outdoor", 0, false),
            Exercise.create("Rowing", MuscleGroup.CARDIO, false, com.vital.data.entity.Equipment.MACHINE, "Full strokes", 0, false),
            Exercise.create("Jump Rope", MuscleGroup.CARDIO, false, com.vital.data.entity.Equipment.OTHER, "Single or double unders", 0, false),

            // FULL BODY
            Exercise.create("Burpees", MuscleGroup.FULL_BODY, false, com.vital.data.entity.Equipment.BODYWEIGHT, "Chest to floor, jump up", 60, true),
            Exercise.create("Kettlebell Swing", MuscleGroup.FULL_BODY, false, com.vital.data.entity.Equipment.KETTLEBELL, "Hip hinge, swing to chest", 90, true),
            Exercise.create("Clean and Press", MuscleGroup.FULL_BODY, true, com.vital.data.entity.Equipment.BARBELL, "Explosive pull to press", 180, true),
            Exercise.create("Thruster", MuscleGroup.FULL_BODY, true, com.vital.data.entity.Equipment.BARBELL, "Front squat to press", 120, true)
        )
        exerciseRepository.insertExercises(exercises)
    }
}