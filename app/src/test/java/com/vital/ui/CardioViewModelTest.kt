package com.vital.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardioViewModelTest {

    @Test
    fun `duration parses mm ss`() {
        assertEquals(32 * 60 + 10, parseCardioDuration("32:10"))
    }

    @Test
    fun `duration parses h mm ss`() {
        assertEquals(3_600 + 2 * 60 + 10, parseCardioDuration("1:02:10"))
    }

    @Test
    fun `bare number is minutes`() {
        assertEquals(45 * 60, parseCardioDuration("45"))
    }

    @Test
    fun `invalid durations return null`() {
        assertNull(parseCardioDuration(""))
        assertNull(parseCardioDuration("32:70"))
        assertNull(parseCardioDuration("1:70:10"))
        assertNull(parseCardioDuration("0"))
        assertNull(parseCardioDuration("a:b"))
    }

    @Test
    fun `acsm estimate uses body weight and clamps above six`() {
        val atSix = estimateCardioKcal(6.0, 2.0, 68.0, 32 * 60 + 10)
        assertEquals(187.0, atSix!!, 0.5)

        val aboveSix = estimateCardioKcal(8.0, 2.0, 68.0, 32 * 60 + 10)
        assertEquals(atSix, aboveSix!!, 0.001)
    }

    @Test
    fun `estimate needs a speed`() {
        assertNull(estimateCardioKcal(null, 0.0, 68.0, 1800))
        assertNull(estimateCardioKcal(0.0, 0.0, 68.0, 1800))
    }

    @Test
    fun `date formats to dd MM`() {
        assertEquals("30.10", formatCardioDate("2026-10-30"))
        assertEquals("bad", formatCardioDate("bad"))
    }
}
