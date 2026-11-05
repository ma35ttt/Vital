package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeksStatsTest {

    private val today = LocalDate.of(2026, 11, 5)

    private fun day(offset: Int, kcal: Double = 0.0, protein: Double = 0.0) =
        DayRecord(date = today.minusDays(offset.toLong()), kcal = kcal, protein = protein)

    @Test
    fun `empty window is zero adherence`() {
        val snapshot = weeksSnapshot(
            records = emptyList(),
            today = today,
            periodDays = 28,
            barCount = 4,
            proteinTarget = 180,
            carbsTarget = 250,
            fatTarget = 85
        )

        assertEquals(0, snapshot.adherencePct)
        assertEquals(0, snapshot.trackedDays)
        assertEquals(listOf(0, 0, 0, 0), snapshot.bars)
        assertEquals(0, snapshot.proteinPct)
    }

    @Test
    fun `fully tracked window is one hundred percent every bar`() {
        val records = (0 until 28).map { day(it, kcal = 2_400.0) }

        val snapshot = weeksSnapshot(records, today, 28, 4, 180, 250, 85)

        assertEquals(100, snapshot.adherencePct)
        assertEquals(listOf(100, 100, 100, 100), snapshot.bars)
    }

    @Test
    fun `records outside the window are ignored`() {
        val records = listOf(
            day(0, kcal = 2_000.0),
            day(28, kcal = 2_000.0),   // one day before the 28-day window starts
            day(40, kcal = 2_000.0)
        )

        val snapshot = weeksSnapshot(records, today, 28, 4, 180, 250, 85)

        assertEquals(1, snapshot.trackedDays)
        assertEquals(4, snapshot.adherencePct) // 1 / 28 rounds to 4
    }

    @Test
    fun `bars bucket the window oldest first`() {
        // Days 0-6 (last week) tracked, everything else empty.
        val records = (0 until 7).map { day(it, kcal = 2_000.0) }

        val snapshot = weeksSnapshot(records, today, 28, 4, 180, 250, 85)

        assertEquals(listOf(0, 0, 0, 100), snapshot.bars)
        assertEquals(25, snapshot.adherencePct)
    }

    @Test
    fun `seven day window draws seven daily bars`() {
        val records = listOf(day(0, kcal = 2_000.0), day(2, kcal = 2_000.0))

        val snapshot = weeksSnapshot(records, today, 7, 7, 180, 250, 85)

        assertEquals(listOf(0, 0, 0, 0, 100, 0, 100), snapshot.bars)
    }

    @Test
    fun `a lift without food counts as a tracked day`() {
        val records = listOf(DayRecord(date = today, lifted = true))

        val snapshot = weeksSnapshot(records, today, 7, 7, 180, 250, 85)

        assertEquals(1, snapshot.trackedDays)
        assertEquals(14, snapshot.adherencePct)
    }

    @Test
    fun `macro hit rate is measured over days with food`() {
        val records = listOf(
            day(0, kcal = 2_000.0, protein = 170.0), // hit (> 180 * 0.9)
            day(1, kcal = 2_000.0, protein = 100.0), // short
            day(2, kcal = 2_000.0, protein = 200.0)  // hit
        )

        val snapshot = weeksSnapshot(records, today, 28, 4, 180, 250, 85)

        assertEquals(67, snapshot.proteinPct)
        assertEquals(0, snapshot.carbsPct)
        assertEquals(0, snapshot.fatPct)
    }

    @Test
    fun `no food days means no macro percentage`() {
        val records = (0 until 14).map { DayRecord(date = today.minusDays(it.toLong()), lifted = true) }

        val snapshot = weeksSnapshot(records, today, 28, 4, 180, 250, 85)

        assertEquals(50, snapshot.adherencePct)
        assertEquals(0, snapshot.proteinPct)
    }

    @Test
    fun `percentages never exceed one hundred`() {
        val records = (0 until 84).map { day(it, kcal = 2_400.0, protein = 400.0) }

        val snapshot = weeksSnapshot(records, today, 84, 12, 180, 250, 85)

        assertEquals(100, snapshot.adherencePct)
        assertTrue(snapshot.proteinPct <= 100)
        assertTrue(snapshot.bars.all { it in 0..100 })
    }
}
