package com.vital.data.seed

import com.vital.data.entity.Equipment
import com.vital.data.entity.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExerciseCatalogTest {

    @Test
    fun `maps a catalog row onto an exercise`() {
        val json = """
            { "version": 1, "exercises": [
              { "name": "Barbell Bench Press", "muscleGroup": "CHEST", "equipment": "BARBELL",
                "isBarbell": true, "instructions": "Press", "defaultRestSec": 180, "isCompound": true }
            ] }
        """.trimIndent()

        val exercises = ExerciseCatalog.parse(json)

        assertEquals(1, exercises.size)
        val exercise = exercises.single()
        assertEquals("Barbell Bench Press", exercise.name)
        assertEquals(MuscleGroup.CHEST, exercise.muscleGroup)
        assertEquals(Equipment.BARBELL, exercise.equipment)
        assertTrue(exercise.isBarbell)
        assertTrue(exercise.isCompound)
        assertEquals(180, exercise.defaultRestSec)
        assertEquals("seed:barbell-bench-press", exercise.id)
    }

    @Test
    fun `ids are stable and unique even for clashing names`() {
        val json = """
            { "exercises": [
              { "name": "Dip", "muscleGroup": "ARMS" },
              { "name": "Dip", "muscleGroup": "CHEST" }
            ] }
        """.trimIndent()

        val ids = ExerciseCatalog.parse(json).map { it.id }

        assertEquals(listOf("seed:dip", "seed:dip-2"), ids)
    }

    @Test
    fun `missing equipment defaults to none`() {
        val json = """{ "exercises": [ { "name": "Plank", "muscleGroup": "CORE" } ] }"""
        assertEquals(Equipment.NONE, ExerciseCatalog.parse(json).single().equipment)
    }

    @Test
    fun `the bundled catalog is large, well formed and unique`() {
        val exercises = ExerciseCatalog.parse(bundledCatalog())

        assertTrue("expected a comprehensive catalog, got ${exercises.size}", exercises.size >= 900)
        assertTrue(exercises.all { it.name.isNotBlank() })
        assertEquals(
            exercises.size,
            exercises.map { it.name.trim().lowercase() }.toSet().size
        )
        assertEquals(exercises.size, exercises.map { it.id }.toSet().size)
        assertTrue(exercises.any { it.muscleGroup == MuscleGroup.CHEST })
        assertTrue(exercises.any { it.muscleGroup == MuscleGroup.LEGS })
    }

    private fun bundledCatalog(): String {
        val candidates = listOf(
            File("src/main/assets/exercises.json"),
            File("app/src/main/assets/exercises.json")
        )
        return candidates.first { it.exists() }.readText()
    }
}
