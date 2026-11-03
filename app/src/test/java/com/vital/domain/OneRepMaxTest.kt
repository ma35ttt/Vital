package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class OneRepMaxTest {

    @Test
    fun `epley estimate for five reps`() {
        assertEquals(116.67, OneRepMax.epley(100.0, 5), 0.01)
    }

    @Test
    fun `epley estimate for ten reps`() {
        assertEquals(133.33, OneRepMax.epley(100.0, 10), 0.01)
    }

    @Test
    fun `zero reps is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            OneRepMax.epley(100.0, 0)
        }
    }
}
