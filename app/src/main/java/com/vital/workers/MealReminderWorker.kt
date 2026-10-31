package com.vital.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vital.R
import com.vital.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MealReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = CoroutineScope(Dispatchers.IO).async {
        val context = applicationContext

        val prefs = context.getSharedPreferences("vital_settings", Context.MODE_PRIVATE)
        val mealRemindersEnabled = prefs.getBoolean("meal_reminders_enabled", true)

        if (!mealRemindersEnabled) {
            Result.success()
        } else {
            val mealIndex = inputData.getInt("meal_index", 0)
            val mealType = when (mealIndex) {
                0 -> "Breakfast"
                1 -> "Lunch"
                2 -> "Dinner"
                else -> "Meal"
            }

            val type = inputData.getString("type") ?: "meal"

            if (type == "water") {
                showWaterNotification(context)
            } else {
                showMealNotification(context, mealType)
            }

            Result.success()
        }
    }.await()

    private fun showMealNotification(context: Context, mealType: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "meal_reminder_channel",
                "Meal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log meals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "meals")
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "meal_reminder_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for $mealType! 🍽️")
            .setContentText("Don't forget to log your $mealType")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(3000 + mealType.hashCode(), notification)
    }

    private fun showWaterNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "water_reminder_channel",
                "Water Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reminders to drink water"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "today")
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "water_reminder_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hydration Check 💧")
            .setContentText("Time to drink some water!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(4000, notification)
    }
}