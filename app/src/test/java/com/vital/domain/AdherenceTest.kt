package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AdherenceTest {

    @Test
    fun `period windows match the weeks tab`() {
        assertEquals(7, AdherencePeriod.L.days)
        assertEquals(28, AdherencePeriod.M.days)
        assertEquals(84, AdherencePeriod.H.days)
    }

    @Test
    fun `full adherence is one hundred percent`() {
        assertEquals(100.0, adherencePercent(7, AdherencePeriod.L), 1e-9)
        assertEquals(100.0, adherencePercent(28, AdherencePeriod.M), 1e-9)
        assertEquals(100.0, adherencePercent(84, AdherencePeriod.H), 1e-9)
    }

    @Test
    fun `partial adherence is proportional`() {
        assertEquals(50.0, adherencePercent(14, AdherencePeriod.M), 1e-9)
        assertEquals(50.0, adherencePercent(42, AdherencePeriod.H), 1e-9)
        assertEquals(57.14, adherencePercent(4, AdherencePeriod.L), 0.01)
    }

    @Test
    fun `adherence is clamped to zero and one hundred`() {
        assertEquals(0.0, adherencePercent(-3, AdherencePeriod.L), 1e-9)
        assertEquals(100.0, adherencePercent(99, AdherencePeriod.L), 1e-9)
    }
}
