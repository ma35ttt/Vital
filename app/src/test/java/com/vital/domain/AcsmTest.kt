package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcsmTest {

    @Test
    fun `spec example 6 kmh 2 percent 68 kg 32_10 is 187 kcal`() {
        val kcal = Acsm.kcal(
            speedKmh = 6.0,
            inclinePercent = 2.0,
            weightKg = 68.0,
            durationSeconds = 32 * 60 + 10
        )
        assertEquals(187.0, kcal!!, 0.5)
    }

    @Test
    fun `above walking limit uses the running equation`() {
        // 10 km/h, flat, 68 kg, 30 min: VO2 = 3.5 + 0.2*(10000/60) = 36.83 -> ~12.52 kcal/min.
        val kcal = Acsm.kcal(10.0, 0.0, 68.0, 30 * 60)!!
        assertEquals(375.7, kcal, 1.0)
    }

    @Test
    fun `running burns more than walking at the same speed boundary`() {
        val walk = Acsm.kcal(6.0, 0.0, 70.0, 1800)!!
        val run = Acsm.kcal(6.1, 0.0, 70.0, 1800)!!
        assertTrue(run > walk)
    }

    @Test
    fun `invalid inputs return null`() {
        assertNull(Acsm.kcal(0.0, 0.0, 68.0, 1800))
        assertNull(Acsm.kcal(5.0, 0.0, 0.0, 1800))
        assertNull(Acsm.kcal(5.0, 0.0, 68.0, 0))
    }

    @Test
    fun `incline increases burn`() {
        val flat = Acsm.kcal(5.0, 0.0, 70.0, 1800)!!
        val inclined = Acsm.kcal(5.0, 5.0, 70.0, 1800)!!
        assertEquals(true, inclined > flat)
    }
}
