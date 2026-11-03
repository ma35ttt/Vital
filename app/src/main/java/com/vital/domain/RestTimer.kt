package com.vital.domain

/**
 * Rest-timer state machine. The UI owns the coroutine that calls [tick]; this class is pure
 * so the foreground service and the screen can share the same rules.
 */
data class RestTimerState(
    val totalSeconds: Int,
    val remainingSeconds: Int = totalSeconds,
    val running: Boolean = false
) {
    val isFinished: Boolean get() = remainingSeconds <= 0

    fun start(): RestTimerState = copy(remainingSeconds = totalSeconds, running = totalSeconds > 0)

    fun tick(seconds: Int = 1): RestTimerState {
        if (!running || isFinished || seconds <= 0) return this
        val next = (remainingSeconds - seconds).coerceAtLeast(0)
        return copy(remainingSeconds = next, running = next > 0)
    }

    fun pause(): RestTimerState = copy(running = false)

    fun resume(): RestTimerState =
        if (isFinished) this else copy(running = true)

    fun reset(): RestTimerState = copy(remainingSeconds = totalSeconds, running = false)
}
