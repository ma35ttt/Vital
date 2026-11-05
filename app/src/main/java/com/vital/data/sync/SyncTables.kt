package com.vital.data.sync

import com.vital.data.dao.BodyMetricsDao
import com.vital.data.dao.CardioDao
import com.vital.data.dao.ExerciseDao
import com.vital.data.dao.FoodDao
import com.vital.data.dao.MealLogDao
import com.vital.data.dao.RecipeDao
import com.vital.data.dao.SettingsDao
import com.vital.data.dao.UserDao
import com.vital.data.dao.WaterEntryDao
import com.vital.data.dao.WorkoutDao
import com.vital.data.entity.BodyMetrics
import com.vital.data.entity.CardioSession
import com.vital.data.entity.Exercise
import com.vital.data.entity.Food
import com.vital.data.entity.MealLogEntry
import com.vital.data.entity.Recipe
import com.vital.data.entity.Settings
import com.vital.data.entity.User
import com.vital.data.entity.WaterEntry
import com.vital.data.entity.WorkoutSession
import com.vital.data.entity.WorkoutSet
import com.vital.data.seed.LOCAL_USER_ID

/** Holds every table that participates in sync. */
class SyncTables(val all: List<SyncTable<*>>)

/**
 * The synced tables and how each maps to Room. Progress photos and the seeded catalogs are
 * intentionally excluded: photos are not synced yet, and seeds are re-created on every install.
 * Local user rows keep the fixed [LOCAL_USER_ID], so ids stay stable across devices.
 */
fun buildSyncTables(
    userDao: UserDao,
    foodDao: FoodDao,
    recipeDao: RecipeDao,
    mealLogDao: MealLogDao,
    exerciseDao: ExerciseDao,
    workoutDao: WorkoutDao,
    cardioDao: CardioDao,
    bodyMetricsDao: BodyMetricsDao,
    waterEntryDao: WaterEntryDao,
    settingsDao: SettingsDao
): SyncTables {
    val tables = listOf(
        SyncTable(
            name = "users",
            serializer = User.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { userDao.getUsersForSync(0L) },
            upsertAll = { rows -> rows.forEach { userDao.insertUser(it) } },
            // The PIN hash is a local credential and never leaves the device; on restore we keep
            // whatever the device already has.
            toCloud = { it.copy(pinHash = null) },
            mergeFromCloud = { incoming, existing -> incoming.copy(pinHash = existing?.pinHash) },
            preferCloudOnFirstSync = true
        ),
        SyncTable(
            name = "settings",
            serializer = Settings.serializer(),
            idOf = { it.userId },
            updatedAtOf = { it.updatedAt },
            deletedOf = { false },
            readAll = { listOfNotNull(settingsDao.getSettingsForSync(LOCAL_USER_ID)) },
            upsertAll = { rows -> rows.forEach { settingsDao.insertSettings(it) } },
            // The AI API key is a secret and stays local; a restored device keeps its own key.
            toCloud = { it.copy(aiApiKeyEncrypted = null) },
            mergeFromCloud = { incoming, existing ->
                incoming.copy(aiApiKeyEncrypted = existing?.aiApiKeyEncrypted)
            },
            preferCloudOnFirstSync = true
        ),
        SyncTable(
            name = "body_metrics",
            serializer = BodyMetrics.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { bodyMetricsDao.getBodyMetricsForSync(0L) },
            upsertAll = { rows -> rows.forEach { bodyMetricsDao.insertBodyMetrics(it) } }
        ),
        SyncTable(
            name = "cardio_sessions",
            serializer = CardioSession.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { cardioDao.getCardioSessionsForSync(0L) },
            upsertAll = { rows -> rows.forEach { cardioDao.insertCardioSession(it) } }
        ),
        SyncTable(
            name = "exercises",
            serializer = Exercise.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { exerciseDao.getExercisesForSync(0L) },
            upsertAll = { rows -> exerciseDao.insertExercises(rows) }
        ),
        SyncTable(
            name = "foods",
            serializer = Food.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { foodDao.getFoodsForSync(0L) },
            upsertAll = { rows -> foodDao.insertFoods(rows) }
        ),
        SyncTable(
            name = "meal_log_entries",
            serializer = MealLogEntry.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { mealLogDao.getMealLogsForSync(0L) },
            upsertAll = { rows -> mealLogDao.insertMealLogs(rows) }
        ),
        SyncTable(
            name = "recipes",
            serializer = Recipe.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { recipeDao.getRecipesForSync(0L) },
            upsertAll = { rows -> recipeDao.insertRecipes(rows) }
        ),
        SyncTable(
            name = "water_entries",
            serializer = WaterEntry.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { waterEntryDao.getWaterEntriesForSync(0L) },
            upsertAll = { rows -> waterEntryDao.insertWaterEntries(rows) }
        ),
        SyncTable(
            name = "workout_sessions",
            serializer = WorkoutSession.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { workoutDao.getSessionsForSync(0L) },
            upsertAll = { rows -> rows.forEach { workoutDao.insertSession(it) } }
        ),
        SyncTable(
            name = "workout_sets",
            serializer = WorkoutSet.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { workoutDao.getSetsForSync(0L) },
            upsertAll = { rows -> workoutDao.insertSets(rows) }
        )
    )
    return SyncTables(tables)
}
