package com.vital.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VitalThemesTest {

    @Test
    fun `base is the first three, featured is ten`() {
        assertEquals(3, VitalThemes.base.size)
        assertEquals(10, VitalThemes.featured.size)
        assertEquals(13, VitalThemes.all.size)
        assertEquals(listOf("white", "dark", "amoled"), VitalThemes.base.map { it.id })
    }

    @Test
    fun `ids are unique and names are set`() {
        val ids = VitalThemes.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(VitalThemes.all.all { it.name.isNotBlank() })
    }

    @Test
    fun `default resolves and unknown ids fall back`() {
        assertEquals(VitalThemes.DEFAULT_ID, VitalThemes.byId(VitalThemes.DEFAULT_ID).id)
        assertEquals("ember", VitalThemes.byId("ember").id)
        assertEquals(VitalThemes.DEFAULT_ID, VitalThemes.byId("does-not-exist").id)
    }

    @Test
    fun `light and dark flags match the design`() {
        val light = setOf("white", "sepia", "blush", "glacier", "kraft")
        VitalThemes.all.forEach { theme ->
            assertEquals(theme.id, light.contains(theme.id), theme.isLight)
        }
        assertTrue(VitalThemes.byId("white").isLight)
        assertFalse(VitalThemes.byId("amoled").isLight)
    }
}
