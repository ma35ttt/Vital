package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateCalculatorTest {

    @Test
    fun `80 kg is 25 plus 5 per side`() {
        val load = PlateCalculator.forTarget(80.0)
        assertNotNull(load)
        assertEquals(80.0, load!!.totalKg, 1e-9)
        assertEquals(listOf(25.0, 5.0), load.perSideKg)
    }

    @Test
    fun `100 kg is 25 plus 15 per side`() {
        val load = PlateCalculator.forTarget(100.0)
        assertNotNull(load)
        assertEquals(100.0, load!!.totalKg, 1e-9)
        assertEquals(listOf(25.0, 15.0), load.perSideKg)
    }

    @Test
    fun `bar alone is loadable`() {
        val load = PlateCalculator.forTarget(20.0)
        assertNotNull(load)
        assertEquals(20.0, load!!.totalKg, 1e-9)
        assertTrue(load.perSideKg.isEmpty())
    }

    @Test
    fun `below bar weight is not loadable`() {
        assertNull(PlateCalculator.forTarget(19.0))
    }

    @Test
    fun `odd target is not exactly loadable`() {
        assertNull(PlateCalculator.forTarget(83.0))
    }

    @Test
    fun `nearest loadable resolves 83 to 82_5`() {
        val load = PlateCalculator.nearestLoadable(83.0)
        assertNotNull(load)
        assertEquals(82.5, load!!.totalKg, 1e-9)
        assertEquals(listOf(25.0, 5.0, 1.25), load.perSideKg)
    }

    @Test
    fun `nearest loadable resolves 20_1 to 20`() {
        val load = PlateCalculator.nearestLoadable(20.1)
        assertNotNull(load)
        assertEquals(20.0, load!!.totalKg, 1e-9)
    }

    @Test
    fun `custom plate inventory is respected`() {
        val load = PlateCalculator.forTarget(
            targetKg = 60.0,
            barKg = 20.0,
            plates = listOf(10.0, 5.0)
        )
        assertNotNull(load)
        assertEquals(listOf(10.0, 10.0), load!!.perSideKg)
    }
}
