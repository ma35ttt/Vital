package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrDetectionTest {

    @Test
    fun `first set is always a PR`() {
        val check = detectPr(100.0, 5, emptyList())
        assertTrue(check.isPr)
        assertNull(check.previousBestKg)
        assertEquals(116.67, check.estimated1RmKg, 0.01)
    }

    @Test
    fun `heavier estimated max is a PR`() {
        val check = detectPr(100.0, 5, listOf(110.0 to 1))
        assertTrue(check.isPr)
        assertEquals(113.67, check.previousBestKg!!, 0.01)
    }

    @Test
    fun `equal estimated max is not a PR`() {
        val check = detectPr(100.0, 5, listOf(100.0 to 5))
        assertFalse(check.isPr)
    }

    @Test
    fun `lighter estimated max is not a PR`() {
        val check = detectPr(90.0, 5, listOf(100.0 to 5))
        assertFalse(check.isPr)
    }

    @Test
    fun `history is compared by estimated max not raw weight`() {
        // 100 x 5 = 116.7 beats 110 x 1 = 113.7 even though the raw weight is lower.
        val check = detectPr(100.0, 5, listOf(110.0 to 1))
        assertTrue(check.isPr)
    }
}
