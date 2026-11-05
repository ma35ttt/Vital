package com.vital.data.seed

import com.vital.data.entity.FoodSource
import com.vital.data.entity.FoodVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FoodCatalogTest {

    @Test
    fun `maps a catalog row onto a food`() {
        val json = """
            { "version": 1, "foods": [
              { "name": "Paneer (Chhena)", "hindi": "Chhena", "code": "L002",
                "kcal": 265, "protein": 18.3, "carbs": 1.2, "fat": 20.8,
                "verified": true, "source": "IFCT2017" }
            ] }
        """.trimIndent()

        val foods = FoodCatalog.parse(json)

        assertEquals(1, foods.size)
        val food = foods.single()
        assertEquals("Paneer (Chhena)", food.name)
        assertEquals(FoodVariant.NONE, food.variant)
        assertEquals(FoodSource.SEED, food.source)
        assertTrue(food.verified)
        assertEquals(265.0, food.kcalPer100, 0.0)
        assertEquals(18.3, food.proteinPer100, 0.0)
        assertEquals("seed:paneer-chhena", food.id)
        assertEquals("Chhena · IFCT2017 L002", food.referenceName)
    }

    @Test
    fun `source energy is kept instead of being re-derived`() {
        val json = """
            { "foods": [ { "name": "Ghee", "kcal": 900, "protein": 0.0, "carbs": 0.0, "fat": 100.0 } ] }
        """
        // 4/4/9 would say 900 here too, so use a row where the two disagree.
        val row = """
            { "foods": [ { "name": "Amaranth seed, black", "kcal": 356, "protein": 14.6, "carbs": 60.0, "fat": 5.7 } ] }
        """
        assertEquals(900.0, FoodCatalog.parse(json).single().kcalPer100, 0.0)
        // 4*14.6 + 4*60 + 9*5.7 = 349.7, not the measured 356.
        assertEquals(356.0, FoodCatalog.parse(row).single().kcalPer100, 0.0)
    }

    @Test
    fun `ids are stable and unique even for clashing names`() {
        val json = """
            { "foods": [
              { "name": "Crab, marine", "kcal": 82, "protein": 1, "carbs": 1, "fat": 1 },
              { "name": "Crab, marine", "kcal": 78, "protein": 1, "carbs": 1, "fat": 1 }
            ] }
        """.trimIndent()

        val ids = FoodCatalog.parse(json).map { it.id }

        assertEquals(listOf("seed:crab-marine", "seed:crab-marine-2"), ids)
    }

    @Test
    fun `reference name is null without a hindi name or code`() {
        val json = """{ "foods": [ { "name": "Plain", "kcal": 1, "protein": 0, "carbs": 0, "fat": 0 } ] }"""
        assertNull(FoodCatalog.parse(json).single().referenceName)
    }

    @Test
    fun `the bundled catalog is large, well formed and unique`() {
        val foods = FoodCatalog.parse(bundledCatalog())

        assertTrue("expected a comprehensive catalog, got ${foods.size}", foods.size >= 600)
        assertTrue(foods.all { it.name.isNotBlank() })
        assertTrue(foods.all { it.kcalPer100 > 0.0 })
        assertEquals(foods.size, foods.map { it.name.trim().lowercase() }.toSet().size)
        assertEquals(foods.size, foods.map { it.id }.toSet().size)
        // Indian staples and the IFCT source should both be present.
        assertTrue(foods.any { it.name.contains("dal", ignoreCase = true) })
        assertTrue(foods.any { it.name.contains("paneer", ignoreCase = true) })
        assertTrue(foods.all { it.source == FoodSource.SEED })
    }

    private fun bundledCatalog(): String {
        val candidates = listOf(
            File("src/main/assets/foods.json"),
            File("app/src/main/assets/foods.json")
        )
        return candidates.first { it.exists() }.readText()
    }
}
