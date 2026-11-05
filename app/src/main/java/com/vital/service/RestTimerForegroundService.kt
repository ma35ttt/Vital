package com.vital.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.vital.R
import com.vital.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.max

class RestTimerForegroundService : Service(), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var notificationManager: NotificationManager? = null
    private var timerJob: Job? = null
    private var endTimeMs: Long = 0
    private var totalSeconds: Int = 300
    private var isRunning = false

    companion object {
        const val CHANNEL_ID = "rest_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.vital.ACTION_START_TIMER"
        const val ACTION_PAUSE = "com.vital.ACTION_PAUSE_TIMER"
        const val ACTION_CANCEL = "com.vital.ACTION_CANCEL_TIMER"
        const val ACTION_TICK = "com.vital.ACTION_TIMER_TICK"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val EXTRA_TOTAL_SECONDS = "total_seconds"
        const val EXTRA_REMAINING_SECONDS = "remaining_seconds"
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        intent?.action?.let { action ->
            when (action) {
                ACTION_START -> {
                    val duration = intent.getIntExtra(EXTRA_DURATION_SECONDS, 180)
                    totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, duration)
                        .coerceAtLeast(1)
                    startTimer(duration)
                }
                ACTION_PAUSE -> pauseTimer()
                ACTION_CANCEL -> cancelTimer()
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rest Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active rest timer countdown"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun startTimer(durationSeconds: Int) {
        timerJob?.cancel()
        endTimeMs = SystemClock.elapsedRealtime() + (durationSeconds * 1000L)
        isRunning = true

        // A foreground service must post its notification promptly; do it before the loop.
        updateNotification(durationSeconds)

        timerJob = scope.launch {
            while (isRunning) {
                delay(1000)
                val remaining = max(0, (endTimeMs - SystemClock.elapsedRealtime()) / 1000)
                if (remaining <= 0) {
                    timerComplete()
                    break
                }
                updateNotification(remaining.toInt())
            }
        }
    }

    private fun pauseTimer() {
        isRunning = false
        timerJob?.cancel()
        val remaining = max(0, (endTimeMs - SystemClock.elapsedRealtime()) / 1000)
        updateNotification(remaining.toInt(), isPaused = true)
    }

    private fun cancelTimer() {
        isRunning = false
        timerJob?.cancel()
        stopForeground(true)
        stopSelf()
    }

    private fun timerComplete() {
        isRunning = false
        // Timer completed - could play sound/vibrate here
        updateNotification(0, isComplete = true)

        // Auto-dismiss after 5 seconds
        scope.launch {
            delay(5000)
            stopForeground(true)
            stopSelf()
        }
    }

    private fun updateNotification(remainingSeconds: Int, isPaused: Boolean = false, isComplete: Boolean = false) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)

        val contentTitle = if (isComplete) "REST COMPLETE" else if (isPaused) "REST PAUSED" else "REST TIMER"
        val contentText = if (isComplete) "Ready for next set!" else "$timeString remaining"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = Intent(this, RestTimerForegroundService::class.java).apply {
            action = ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getService(
            this, 0, cancelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseIntent = Intent(this, RestTimerForegroundService::class.java).apply {
            action = if (isRunning) ACTION_PAUSE else ACTION_START
            putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOngoing(!isComplete)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .addAction(
                if (isRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow,
                if (isRunning) "PAUSE" else "RESUME",
                pausePendingIntent
            )
            .addAction(R.drawable.ic_close, "CANCEL", cancelPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isComplete) {
            val fraction = (remainingSeconds * 100 / totalSeconds.coerceAtLeast(1))
                .coerceIn(0, 100)
            builder.setProgress(100, fraction, false)
        }

        val notification = builder.build()
        notificationManager?.notify(NOTIFICATION_ID, notification)

        // Update foreground service notification
        if (!isComplete) {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        scope.cancel()
        timerJob?.cancel()
        super.onDestroy()
    }
}