package com.vital.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TreadmillReadingTest {

    @Test
    fun `drops impossible console values but keeps real ones`() {
        val clean = TreadmillReading(
            durationSec = 99_999,
            distanceKm = 500.0,
            speedKmh = 6.0,
            inclinePct = 2.0,
            confidence = 1.7
        ).sanitized()

        assertNull(clean.durationSec)
        assertNull(clean.distanceKm)
        assertEquals(6.0, clean.speedKmh!!, 0.0)
        assertEquals(2.0, clean.inclinePct!!, 0.0)
        assertEquals(1.0, clean.confidence, 0.0)
    }

    @Test
    fun `keeps an ordinary treadmill reading`() {
        val reading = TreadmillReading(
            durationSec = 1_930,
            distanceKm = 3.22,
            speedKmh = 6.0,
            inclinePct = 2.0,
            confidence = 0.88
        ).sanitized()

        assertEquals(reading, TreadmillReading(1_930, 3.22, 6.0, 2.0, 0.88))
    }

    @Test
    fun `null stays null`() {
        val clean = TreadmillReading(confidence = 0.5).sanitized()
        assertNull(clean.durationSec)
        assertNull(clean.distanceKm)
        assertNull(clean.speedKmh)
        assertNull(clean.inclinePct)
    }
}