package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartTextParserTest {

    @Test
    fun `empty input returns nothing`() {
        val result = parseSmartText("")
        assertTrue(result.items.isEmpty())
        assertTrue(result.ambiguities.isEmpty())
    }

    @Test
    fun `weight after food is parsed`() {
        val result = parseSmartText("chicken 150g")
        assertEquals(1, result.items.size)
        val item = result.items.first()
        assertEquals("chicken breast", item.name)
        assertEquals(150.0, item.grams, 1e-9)
        assertEquals(180.0, item.kcal, 1e-9)
    }

    @Test
    fun `weight before food is parsed`() {
        val result = parseSmartText("150g chicken")
        assertEquals(150.0, result.items.first().grams, 1e-9)
    }

    @Test
    fun `raw or cooked ambiguity is asked once`() {
        val result = parseSmartText("chicken 150g")
        assertEquals(1, result.ambiguities.size)
        val ambiguity = result.ambiguities.first()
        assertEquals("chicken breast", ambiguity.foodName)
        assertEquals(150.0, ambiguity.grams, 1e-9)
        assertEquals("CHICKEN BREAST 150G · RAW OR COOKED?", ambiguity.question)
    }

    @Test
    fun `cooked keyword selects cooked macros and clears ambiguity`() {
        val result = parseSmartText("chicken 150g cooked")
        assertTrue(result.ambiguities.isEmpty())
        val item = result.items.first()
        assertEquals(Preparation.COOKED, item.preparation)
        assertEquals(247.5, item.kcal, 1e-9)
        assertEquals(46.5, item.proteinG, 1e-9)
    }

    @Test
    fun `raw keyword selects raw macros`() {
        val result = parseSmartText("rice 150g raw")
        assertTrue(result.ambiguities.isEmpty())
        assertEquals(Preparation.RAW, result.items.first().preparation)
        assertEquals(540.0, result.items.first().kcal, 1e-9)
    }

    @Test
    fun `units expand through grams per unit`() {
        val result = parseSmartText("2 eggs")
        val item = result.items.first()
        assertEquals(100.0, item.grams, 1e-9)
        assertEquals(2.0, item.quantity!!, 1e-9)
        assertEquals(143.0, item.kcal, 1e-9)
    }

    @Test
    fun `multiple foods are parsed separately`() {
        val result = parseSmartText("chicken breast cooked 200g rice 150g")
        assertEquals(2, result.items.size)
        assertEquals(330.0, result.items[0].kcal, 1e-9)
        assertEquals(540.0, result.items[1].kcal, 1e-9)
        assertEquals(1, result.ambiguities.size)
        assertEquals("rice", result.ambiguities.first().foodName)
    }

    @Test
    fun `totals sum the parsed items`() {
        val result = parseSmartText("2 eggs 100g oats")
        assertEquals(143.0 + 389.0, result.kcal, 1e-9)
        assertEquals(12.6 + 16.9, result.proteinG, 1e-9)
    }

    @Test
    fun `unknown food is ignored`() {
        val result = parseSmartText("pizza 200g")
        assertTrue(result.items.isEmpty())
    }
}
