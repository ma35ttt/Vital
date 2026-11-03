package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `faster than walking limit returns null`() {
        assertNull(Acsm.kcal(6.1, 0.0, 68.0, 1800))
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
