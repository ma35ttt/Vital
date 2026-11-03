package com.vital.domain

/** Adherence windows from the Weeks tab: L = 7 days, M = 4 weeks, H = 12 weeks. */
enum class AdherencePeriod(val days: Int) {
    L(7),
    M(28),
    H(84)
}

fun adherencePercent(loggedDays: Int, period: AdherencePeriod): Double =
    (loggedDays.toDouble() / period.days * 100.0).coerceIn(0.0, 100.0)
