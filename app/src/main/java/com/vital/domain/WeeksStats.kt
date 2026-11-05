package com.vital.domain

import java.time.LocalDate
import kotlin.math.roundToInt

/** One day's real totals, as Weeks needs them. */
data class DayRecord(
    val date: LocalDate,
    val kcal: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val lifted: Boolean = false,
    val cardio: Boolean = false
) {
    /** A day counts as tracked if anything was logged on it, food or training. */
    val tracked: Boolean get() = kcal > 0.0 || lifted || cardio

    val ate: Boolean get() = kcal > 0.0
}

data class WeeksSnapshot(
    val periodDays: Int,
    val trackedDays: Int,
    val proteinPct: Int,
    val carbsPct: Int,
    val fatPct: Int,
    /** Tracked-day percentage per bucket, oldest first; the last bar is the current one. */
    val bars: List<Int>
) {
    val adherencePct: Int
        get() = if (periodDays <= 0) 0 else (trackedDays * 100.0 / periodDays).roundToInt()
}

/** A macro counts as hit at 90% of target; below that the day is still logged, just short. */
private const val MACRO_HIT_RATIO = 0.9

/**
 * Aggregates day records into the Weeks tab's numbers.
 *
 * The window is the [periodDays] ending on [today] (inclusive). Bars split the same window
 * into [barCount] even buckets, so 7D draws seven daily bars while 4W and 12W draw weeks.
 * Macro percentages are among days that had food logged, not all calendar days: a day you
 * did not eat yet should not read as a failed protein day.
 */
fun weeksSnapshot(
    records: List<DayRecord>,
    today: LocalDate,
    periodDays: Int,
    barCount: Int,
    proteinTarget: Int,
    carbsTarget: Int,
    fatTarget: Int
): WeeksSnapshot {
    require(periodDays > 0) { "periodDays must be positive" }
    require(barCount > 0) { "barCount must be positive" }

    val start = today.minusDays((periodDays - 1).toLong())
    val byDate = records
        .filter { !it.date.isBefore(start) && !it.date.isAfter(today) }
        .associateBy { it.date }

    val days = (0 until periodDays).map { start.plusDays(it.toLong()) }
    val tracked = days.count { byDate[it]?.tracked == true }
    val ateDays = days.mapNotNull { byDate[it]?.takeIf { day -> day.ate } }

    fun hit(macro: (DayRecord) -> Double, target: Int): Int {
        if (target <= 0 || ateDays.isEmpty()) return 0
        val hits = ateDays.count { macro(it) >= target * MACRO_HIT_RATIO }
        return (hits * 100.0 / ateDays.size).roundToInt()
    }

    val bucket = (periodDays + barCount - 1) / barCount
    val bars = days.chunked(bucket).map { chunk ->
        if (chunk.isEmpty()) 0
        else (chunk.count { byDate[it]?.tracked == true } * 100.0 / chunk.size).roundToInt()
    }

    return WeeksSnapshot(
        periodDays = periodDays,
        trackedDays = tracked,
        proteinPct = hit({ it.protein }, proteinTarget),
        carbsPct = hit({ it.carbs }, carbsTarget),
        fatPct = hit({ it.fat }, fatTarget),
        bars = bars
    )
}
