package com.vital.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.vital.R
import com.vital.ui.MainActivity
import com.vital.ui.RestTimerController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Keeps a rest countdown alive after the app is closed.
 *
 * Two surfaces point at the same rest: a low-importance foreground notification (which mirrors
 * to any paired watch) and, once the user has left the app and granted "Display over other
 * apps", a small always-on-top chip. Both are driven by [RestTimerController] through
 * [RestTimerRelay], so the in-app line, the chip and the notification never disagree.
 */
@AndroidEntryPoint
class RestTimerForegroundService : Service(), LifecycleOwner {

    @Inject
    lateinit var appVisibility: AppVisibility

    @Inject
    lateinit var restTimerController: RestTimerController

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var notificationManager: NotificationManager? = null
    private var windowManager: WindowManager? = null
    private var timerJob: Job? = null
    private var visibilityJob: Job? = null
    private var endTimeMs: Long = 0
    private var totalSeconds: Int = 300
    private var isRunning = false
    private var promotionLogged = false

    // The orange version of the same transparent mark, shown in the expanded notification.
    private val largeIcon by lazy {
        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
    }

    // The floating chip over other apps; null whenever it is not attached.
    private var overlayView: View? = null
    private var overlayTime: TextView? = null

    companion object {
        private const val TAG = "RestTimer"
        const val CHANNEL_ID = "rest_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.vital.ACTION_START_TIMER"
        const val ACTION_CANCEL = "com.vital.ACTION_CANCEL_TIMER"
        const val ACTION_EXTEND = "com.vital.ACTION_EXTEND_TIMER"
        const val ACTION_DONE = "com.vital.ACTION_DONE_TIMER"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val EXTRA_TOTAL_SECONDS = "total_seconds"
        private const val EXTEND_SECONDS = 15
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()

        // The chip is the out-of-app surface only: show it the moment Vital leaves the screen,
        // drop it the moment it comes back.
        visibilityJob = scope.launch {
            appVisibility.isForeground.collect { foreground ->
                if (foreground) hideOverlay() else syncOverlay()
            }
        }
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
                ACTION_CANCEL -> cancelTimer()
                // Notification controls go back through the controller so the in-app line and
                // the chip stay in step with what the notification shows.
                ACTION_EXTEND -> restTimerController.extend(EXTEND_SECONDS)
                ACTION_DONE -> restTimerController.stop()
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
        promotionLogged = false

        // A foreground service must post its notification promptly; do it before the loop.
        updateNotification(durationSeconds)

        timerJob = scope.launch {
            while (isRunning) {
                delay(1000)
                val remaining = currentRemaining()
                if (remaining <= 0) {
                    timerComplete()
                    break
                }
                updateNotification(remaining)
            }
        }
    }

    private fun cancelTimer() {
        isRunning = false
        timerJob?.cancel()
        hideOverlay()
        stopForeground(true)
        stopSelf()
    }

    private fun timerComplete() {
        isRunning = false
        // Completion could play a sound or vibrate here.
        updateNotification(0, isComplete = true)

        // Auto-dismiss after 5 seconds.
        scope.launch {
            delay(5000)
            hideOverlay()
            stopForeground(true)
            stopSelf()
        }
    }

    private fun updateNotification(remainingSeconds: Int, isComplete: Boolean = false) {
        val timeString = formatClock(remainingSeconds)

        val contentTitle = if (isComplete) "REST COMPLETE" else "REST TIMER"
        val contentText = if (isComplete) "Ready for next set!" else "$timeString remaining"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val extendIntent = Intent(this, RestTimerForegroundService::class.java).apply {
            action = ACTION_EXTEND
        }
        val extendPendingIntent = PendingIntent.getService(
            this, 1, extendIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val doneIntent = Intent(this, RestTimerForegroundService::class.java).apply {
            action = ACTION_DONE
        }
        val donePendingIntent = PendingIntent.getService(
            this, 2, doneIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vital_status)
            .setLargeIcon(largeIcon)
            .setColor(0xFFFF4D00.toInt())
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOngoing(!isComplete)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setUsesChronometer(false)

        if (!isComplete) {
            builder.addAction(R.drawable.ic_plus, "+15s", extendPendingIntent)
            builder.addAction(R.drawable.ic_close, "DONE", donePendingIntent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isComplete) {
            val fraction = (remainingSeconds * 100 / totalSeconds.coerceAtLeast(1))
                .coerceIn(0, 100)
            builder.setProgress(100, fraction, false)
        }

        // Android 16 Live Updates: ask the system to promote this ongoing notification, which
        // gives it a chip in the status bar (no overlay permission needed). The framework
        // setters are API 36.1, so at compileSdk 36 the documented string extras are used;
        // setShortCriticalText itself is just the first extra. A standard style with progress
        // is promotable, and the chip text must be short (the clock fits in 7 characters).
        if (Build.VERSION.SDK_INT >= 36 && !isComplete) {
            builder.addExtras(Bundle().apply {
                putString("android.shortCriticalText", timeString)
                putBoolean("android.requestPromotedOngoing", true)
            })
        }

        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= 36 && !isComplete && !promotionLogged) {
            promotionLogged = true
            Log.d(
                TAG,
                "rest notification promotable=${notification.hasPromotableCharacteristics()} " +
                    "allowed=${notificationManager?.canPostPromotedNotifications() == true}"
            )
        }
        notificationManager?.notify(NOTIFICATION_ID, notification)

        // Update foreground service notification.
        if (!isComplete) {
            startForeground(NOTIFICATION_ID, notification)
        }

        syncOverlay()
    }

    // ---- floating chip over other apps ------------------------------------------------------

    /** Attaches, updates or removes the chip to match the current rest and app visibility. */
    private fun syncOverlay() {
        // When the system is already showing the rest as a Live Update chip, the floating
        // chip would just duplicate it. Prefer the native surface and keep the overlay as
        // the fallback for devices/users without promotion.
        val shouldShow = isRunning && !appVisibility.isForeground.value &&
            Settings.canDrawOverlays(this) && !isPromoted()
        if (!shouldShow) {
            hideOverlay()
            return
        }
        if (overlayView == null) showOverlay()
        overlayTime?.text = formatClock(currentRemaining())
    }

    /** True when the system has promoted our rest notification into a status-bar chip. */
    private fun isPromoted(): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        val active = runCatching { notificationManager?.activeNotifications }.getOrNull()
            ?: return false
        return active.any { entry ->
            entry.id == NOTIFICATION_ID &&
                (entry.notification.flags and Notification.FLAG_PROMOTED_ONGOING) != 0
        }
    }

    private fun showOverlay() {
        if (overlayView != null || !Settings.canDrawOverlays(this)) return
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = statusBarHeightPx() + dpToPx(6)
        }
        val view = buildOverlayView()
        runCatching { windowManager?.addView(view, params) }.onSuccess {
            overlayView = view
        }
    }

    private fun hideOverlay() {
        val view = overlayView ?: return
        runCatching { windowManager?.removeView(view) }
        overlayView = null
        overlayTime = null
    }

    /** A black pill with the brand mark and the remaining time — the same shape as the mock. */
    private fun buildOverlayView(): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(6), dpToPx(13), dpToPx(6))
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(16).toFloat()
                setColor(0xFF050505.toInt())
                setStroke(dpToPx(1), 0xFF242220.toInt())
            }
            elevation = dpToPx(6).toFloat()
        }
        val mark = ImageView(this).apply {
            setImageResource(R.drawable.ic_vital_mark)
            layoutParams = LinearLayout.LayoutParams(dpToPx(16), dpToPx(16))
        }
        val time = TextView(this).apply {
            typeface = Typeface.MONOSPACE
            setTextColor(0xFFF3F1ED.toInt())
            textSize = 13f
            letterSpacing = 0.06f
            setPadding(dpToPx(8), 0, 0, 0)
            text = formatClock(currentRemaining())
        }
        container.addView(mark)
        container.addView(time)
        overlayTime = time
        return container
    }

    private fun currentRemaining(): Int =
        max(0L, (endTimeMs - SystemClock.elapsedRealtime()) / 1000L).toInt()

    private fun formatClock(seconds: Int): String {
        val safe = max(0, seconds)
        return String.format("%d:%02d", safe / 60, safe % 60)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun statusBarHeightPx(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else dpToPx(24)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        hideOverlay()
        visibilityJob?.cancel()
        timerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
