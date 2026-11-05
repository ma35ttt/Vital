package com.vital.domain

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * The "something needs you" rules behind the dot on the More tab.
 *
 * The dot is deliberately conservative: it only lights up when a reminder the user turned on
 * has passed and the thing it reminds them to log is still missing. A reminder that has not
 * come round yet, a disabled reminder, or a day that is already logged stays quiet.
 */
object Reminders {

    /** Parses `["08:00","13:00"]`, dropping anything malformed. */
    fun times(json: String): List<LocalTime> =
        Regex("\"([0-9]{1,2}):([0-9]{2})\"").findAll(json)
            .mapNotNull { match ->
                val hour = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
                val minute = match.groupValues[2].toIntOrNull() ?: return@mapNotNull null
                runCatching { LocalTime.of(hour, minute) }.getOrNull()
            }
            .toList()

    /** Parses `[1,2,3,4,5]` (1 = Monday … 7 = Sunday), dropping out-of-range values. */
    fun days(json: String): List<Int> =
        Regex("\\d+").findAll(json)
            .map { it.value.toInt() }
            .filter { it in 1..7 }
            .toList()

    /** 1 = Monday … 7 = Sunday, matching the stored reminder days. */
    /** Parses a stored `HH:mm` clock time, or null when it is malformed. */
    fun time(value: String): LocalTime? {
        val match = Regex("([0-9]{1,2}):([0-9]{2})").matchEntire(value.trim()) ?: return null
        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: return null
        return runCatching { LocalTime.of(hour, minute) }.getOrNull()
    }

    fun isoDay(day: DayOfWeek): Int = day.value

    /**
     * Meal reminder is due when at least one configured time has passed today and nothing
     * has been logged at all. Logging any meal clears it for the rest of the day.
     */
    fun mealsDue(
        enabled: Boolean,
        times: List<LocalTime>,
        hasMealsToday: Boolean,
        now: LocalTime
    ): Boolean {
        if (!enabled || hasMealsToday || times.isEmpty()) return false
        return times.any { !it.isAfter(now) }
    }

    /** Workout is due on a selected weekday once its time has passed, if nothing was logged. */
    fun workoutDue(
        enabled: Boolean,
        days: List<Int>,
        time: LocalTime?,
        dayOfWeek: Int,
        hasWorkoutToday: Boolean,
        now: LocalTime
    ): Boolean {
        if (!enabled || hasWorkoutToday || time == null) return false
        if (dayOfWeek !in days) return false
        return !time.isAfter(now)
    }
}
