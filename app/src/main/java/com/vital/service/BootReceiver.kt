package com.vital.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.vital.workers.DailyReminderWorker
import com.vital.workers.MealReminderWorker
import com.vital.workers.WorkoutReminderWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            // Reschedule all periodic workers
            reschedulePeriodicWorks(context)
        }
    }

    private fun reschedulePeriodicWorks(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Daily reminder at 8:00 AM
        val dailyReminder = PeriodicWorkRequest.Builder(DailyReminderWorker::class.java, 24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(8, 0), TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminder
        )

        // Meal reminders (breakfast 8:00, lunch 13:00, dinner 19:00)
        val mealTimes = arrayOf(8 to 0, 13 to 0, 19 to 0)
        mealTimes.forEachIndexed { index, (hour, minute) ->
            val mealReminder = PeriodicWorkRequest.Builder(MealReminderWorker::class.java, 24, TimeUnit.HOURS)
                .setInitialDelay(calculateInitialDelay(hour, minute), TimeUnit.MILLISECONDS)
                .addTag("meal_reminder_$index")
                .setInputData(androidx.work.Data.Builder().putInt("meal_index", index).build())
                .build()
            workManager.enqueueUniquePeriodicWork(
                "meal_reminder_$index",
                ExistingPeriodicWorkPolicy.KEEP,
                mealReminder
            )
        }

        // Workout reminder at 18:00 (6 PM)
        val workoutReminder = PeriodicWorkRequest.Builder(WorkoutReminderWorker::class.java, 24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(18, 0), TimeUnit.MILLISECONDS)
            .addTag("workout_reminder")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "workout_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workoutReminder
        )

        // Water reminder every 2 hours during waking hours (8 AM - 10 PM)
        for (hour in 8..22 step 2) {
            val waterReminder = PeriodicWorkRequest.Builder(MealReminderWorker::class.java, 24, TimeUnit.HOURS)
                .setInitialDelay(calculateInitialDelay(hour, 0), TimeUnit.MILLISECONDS)
                .addTag("water_reminder_$hour")
                .setInputData(androidx.work.Data.Builder().putString("type", "water").putInt("hour", hour).build())
                .build()
            workManager.enqueueUniquePeriodicWork(
                "water_reminder_$hour",
                ExistingPeriodicWorkPolicy.KEEP,
                waterReminder
            )
        }
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, targetHour)
            set(java.util.Calendar.MINUTE, targetMinute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (target.timeInMillis <= now.timeInMillis) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}