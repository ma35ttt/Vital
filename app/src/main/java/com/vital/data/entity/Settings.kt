package com.vital.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "settings")
@Serializable
data class Settings(
    @PrimaryKey
    val userId: String,
    // Nutrition targets
    val kcalTarget: Int = 2500,
    val proteinTarget: Int = 180,
    val carbsTarget: Int = 250,
    val fatTarget: Int = 85,
    val bodyWeightKg: Double = 75.0,
    // AI settings
    val aiProvider: AiProvider = AiProvider.GEMINI,
    val aiApiKeyEncrypted: String? = null,
    // Appearance
    val themeId: String = "dark",
    val trueBlack: Boolean = true,
    val lightMode: Boolean = false,
    val themeColor: String = "fudPink",
    // Rest timer
    val restPresetSec: Int = 180,
    // Notifications
    val notificationsEnabled: Boolean = true,
    val dailyReminderTime: String = "08:00", // HH:mm
    val mealRemindersEnabled: Boolean = true,
    val mealReminderTimesJson: String = """["08:00","13:00","19:00","22:00"]""",
    val workoutRemindersEnabled: Boolean = true,
    val workoutReminderDaysJson: String = """[1,2,3,4,5,6,7]""", // 1=Mon...7=Sun
    val workoutReminderTime: String = "18:00",
    val waterRemindersEnabled: Boolean = true,
    val waterReminderIntervalMin: Int = 120,
    // Sync
    val lastBackupAt: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(userId: String): Settings {
            return Settings(userId = userId)
        }
    }
}

enum class AiProvider {
    GEMINI, OPENROUTER
}