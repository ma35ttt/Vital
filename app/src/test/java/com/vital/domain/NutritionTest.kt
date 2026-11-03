package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionTest {

    @Test
    fun `macro kcal uses 4-4-9`() {
        val macros = Macros(proteinG = 10.0, carbsG = 20.0, fatG = 5.0)
        assertEquals(40.0 + 80.0 + 45.0, macros.kcal, 1e-9)
    }

    @Test
    fun `totals add entries`() {
        val entries = listOf(
            Macros(10.0, 20.0, 5.0),
            Macros(5.0, 10.0, 2.0)
        )
        assertEquals(15.0, totalMacros(entries).proteinG, 1e-9)
        assertEquals(30.0, totalMacros(entries).carbsG, 1e-9)
        assertEquals(7.0, totalMacros(entries).fatG, 1e-9)
        assertEquals(165.0 + 78.0, totalKcal(entries), 1e-9)
    }

    @Test
    fun `macro share sums to one hundred`() {
        val share = macroShare(Macros(150.0, 200.0, 60.0))
        assertEquals(100.0, share.proteinPct + share.carbsPct + share.fatPct, 1e-9)
        assertEquals(30.93, share.proteinPct, 0.01)
        assertEquals(41.24, share.carbsPct, 0.01)
        assertEquals(27.84, share.fatPct, 0.01)
    }

    @Test
    fun `zero macros give zero share`() {
        assertEquals(MacroShare(0.0, 0.0, 0.0), macroShare(Macros()))
    }
}
