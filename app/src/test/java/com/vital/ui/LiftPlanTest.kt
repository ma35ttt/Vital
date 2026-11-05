package com.vital.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class LiftPlanTest {

    @Test
    fun `rpe reads whole when it is whole`() {
        assertEquals("8", formatRpe(8.0))
        assertEquals("10", formatRpe(10.0))
    }

    @Test
    fun `rpe keeps a half step`() {
        assertEquals("7.5", formatRpe(7.5))
    }

    @Test
    fun `relative age labels the last workout`() {
        val zone = ZoneId.systemDefault()
        fun daysAgo(days: Long): Long =
            LocalDate.now().minusDays(days).atStartOfDay(zone).toInstant().toEpochMilli()

        assertEquals("Today", relativeAgeLabel(System.currentTimeMillis()))
        assertEquals("Yesterday", relativeAgeLabel(daysAgo(1)))
        assertEquals("3 days ago", relativeAgeLabel(daysAgo(3)))
    }
}