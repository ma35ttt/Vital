package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RecipeSplitterTest {

    private val recipe = RecipeTotals(
        kcal = 1000.0,
        proteinG = 100.0,
        carbsG = 50.0,
        fatG = 20.0
    )

    @Test
    fun `three servings add back to the recipe totals`() {
        val portions = splitRecipe(recipe, 3)
        assertEquals(3, portions.size)
        assertEquals(1000.0, portions.sumOf { it.kcal }, 1e-6)
        assertEquals(100.0, portions.sumOf { it.proteinG }, 1e-6)
        assertEquals(50.0, portions.sumOf { it.carbsG }, 1e-6)
        assertEquals(20.0, portions.sumOf { it.fatG }, 1e-6)
    }

    @Test
    fun `rounding remainder goes to the first portions`() {
        val portions = splitRecipe(recipe, 3)
        assertEquals(333.4, portions[0].kcal, 1e-6)
        assertEquals(333.3, portions[1].kcal, 1e-6)
        assertEquals(333.3, portions[2].kcal, 1e-6)
    }

    @Test
    fun `even split is exact`() {
        val portions = splitRecipe(recipe, 2)
        assertEquals(500.0, portions[0].kcal, 1e-6)
        assertEquals(500.0, portions[1].kcal, 1e-6)
    }

    @Test
    fun `per serving returns the first portion`() {
        assertEquals(333.4, perServing(recipe, 3).kcal, 1e-6)
    }

    @Test
    fun `zero servings is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            splitRecipe(recipe, 0)
        }
    }
}
