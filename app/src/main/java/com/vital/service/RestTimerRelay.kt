package com.vital.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges the in-app [com.vital.ui.RestTimerController] to [RestTimerForegroundService].
 *
 * The controller stays the single source of truth for the island and the Lift screen; every
 * change is mirrored here so a foreground notification keeps the countdown alive after the app
 * is closed. [running] avoids waking the service just to cancel a rest that already finished.
 */
@Singleton
class RestTimerRelay @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var running = false

    /** Starts (or, on an extend, restarts) the foreground countdown. */
    fun start(remainingSeconds: Int, totalSeconds: Int = remainingSeconds) {
        if (remainingSeconds <= 0) return
        running = true
        val intent = Intent(context, RestTimerForegroundService::class.java).apply {
            action = RestTimerForegroundService.ACTION_START
            putExtra(RestTimerForegroundService.EXTRA_DURATION_SECONDS, remainingSeconds)
            putExtra(RestTimerForegroundService.EXTRA_TOTAL_SECONDS, totalSeconds)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    /** Skips the rest; a no-op when the service is not showing a timer. */
    fun cancel() {
        if (!running) return
        running = false
        val intent = Intent(context, RestTimerForegroundService::class.java).apply {
            action = RestTimerForegroundService.ACTION_CANCEL
        }
        runCatching { context.startService(intent) }
    }
}
