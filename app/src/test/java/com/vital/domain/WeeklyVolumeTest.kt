package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyVolumeTest {

    @Test
    fun `volume is weight times reps`() {
        val sets = listOf(
            VolumeSet(100.0, 5),
            VolumeSet(80.0, 10),
            VolumeSet(60.0, 12)
        )
        assertEquals(500.0 + 800.0 + 720.0, weeklyVolume(sets), 1e-9)
    }

    @Test
    fun `volume groups by week`() {
        val sets = listOf(
            VolumeSet(100.0, 5, weekIndex = 0),
            VolumeSet(80.0, 10, weekIndex = 0),
            VolumeSet(90.0, 5, weekIndex = 1)
        )
        val byWeek = volumeByWeek(sets)
        assertEquals(1300.0, byWeek.getValue(0), 1e-9)
        assertEquals(450.0, byWeek.getValue(1), 1e-9)
    }

    @Test
    fun `empty input is zero`() {
        assertEquals(0.0, weeklyVolume(emptyList()), 1e-9)
        assertEquals(emptyMap<Int, Double>(), volumeByWeek(emptyList()))
    }
}
