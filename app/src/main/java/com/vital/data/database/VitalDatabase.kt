package com.vital.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vital.data.dao.*
import com.vital.data.entity.*
import com.vital.data.seed.Converters

@Database(
    entities = [
        User::class,
        Food::class,
        Recipe::class,
        MealLogEntry::class,
        Exercise::class,
        WorkoutSession::class,
        WorkoutSet::class,
        CardioSession::class,
        BodyMetrics::class,
        ProgressPhoto::class,
        WaterEntry::class,
        Settings::class,
        SyncMeta::class,
        SyncCursor::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VitalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun cardioDao(): CardioDao
    abstract fun bodyMetricsDao(): BodyMetricsDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun settingsDao(): SettingsDao
    abstract fun syncMetaDao(): SyncMetaDao
    abstract fun syncCursorDao(): SyncCursorDao

    companion object {
        @Volatile
        private var INSTANCE: VitalDatabase? = null

        /** v2 adds Settings.lightMode; existing rows keep dark mode. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN lightMode INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v3 adds the per-activity cardio fields; existing sessions keep them as null. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cardio_sessions ADD COLUMN level INTEGER")
                db.execSQL("ALTER TABLE cardio_sessions ADD COLUMN cadenceSpm REAL")
                db.execSQL("ALTER TABLE cardio_sessions ADD COLUMN floors INTEGER")
                db.execSQL("ALTER TABLE cardio_sessions ADD COLUMN laps INTEGER")
                db.execSQL("ALTER TABLE cardio_sessions ADD COLUMN stroke TEXT")
                db.execSQL("ALTER TABLE cardio_sessions ADD COLUMN intensity TEXT")
            }
        }

        /**
         * v4 adds Settings.themeId. Existing rows are mapped from the old light-mode flag so
         * nobody's appearance changes: light stayed light, dark stayed dark. The true-black
         * flag is kept alongside the theme.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN themeId TEXT NOT NULL DEFAULT 'dark'")
                db.execSQL(
                    "UPDATE settings SET themeId = CASE WHEN lightMode = 1 THEN 'white' ELSE 'dark' END"
                )
            }
        }

        /**
         * v5 adds Settings.highRefreshRate. High-refresh panels are the common case, so
         * existing rows opt in and keep the smoother motion.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE settings ADD COLUMN highRefreshRate INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        /**
         * v6 adds the per-table sync cursors used by cloud sync. The table starts empty; the
         * first sync pushes everything and records the high-water marks.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `sync_cursors` (" +
                        "`tableName` TEXT NOT NULL, " +
                        "`lastPullAt` INTEGER NOT NULL, " +
                        "`lastPushAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`tableName`))"
                )
            }
        }

        /**
         * v7 adds the body-goal fields used to estimate maintenance and the daily target. Every
         * column is nullable or defaulted, so existing rows keep eating exactly what they did
         * before until the user sets a goal.
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN heightCm REAL NOT NULL DEFAULT 178.0")
                db.execSQL("ALTER TABLE settings ADD COLUMN age INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE settings ADD COLUMN sex TEXT NOT NULL DEFAULT 'MALE'")
                db.execSQL(
                    "ALTER TABLE settings ADD COLUMN activityLevel TEXT NOT NULL DEFAULT 'MODERATE'"
                )
                db.execSQL("ALTER TABLE settings ADD COLUMN goalType TEXT NOT NULL DEFAULT 'MAINTAIN'")
                db.execSQL("ALTER TABLE settings ADD COLUMN goalWeightKg REAL")
                db.execSQL(
                    "ALTER TABLE settings ADD COLUMN goalRateKgPerWeek REAL NOT NULL DEFAULT 0.5"
                )
            }
        }

        fun getDatabase(context: Context): VitalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VitalDatabase::class.java,
                    "vital.db"
                ).addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                    MIGRATION_6_7
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}