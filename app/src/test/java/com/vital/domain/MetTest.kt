package com.vital.domain

import com.vital.data.entity.CardioType
import com.vital.data.entity.Intensity
import com.vital.data.entity.StrokeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetTest {

    @Test
    fun `cycling at seven MET for forty minutes`() {
        // 7.0 * 3.5 * 68 / 200 = 8.33 kcal/min, times 40 minutes.
        assertEquals(333.2, Met.kcal(CardioType.CYCLING, 68.0, 40 * 60)!!, 0.5)
    }

    @Test
    fun `swim stroke chooses the MET`() {
        assertEquals(6.0, Met.value(CardioType.SWIMMING, stroke = StrokeType.FREESTYLE)!!, 0.0)
        assertEquals(5.3, Met.value(CardioType.SWIMMING, stroke = StrokeType.BREASTSTROKE)!!, 0.0)
        // No stroke defaults to freestyle.
        assertEquals(6.0, Met.value(CardioType.SWIMMING)!!, 0.0)
    }

    @Test
    fun `other uses the effort picker`() {
        assertEquals(3.0, Met.value(CardioType.OTHER, intensity = Intensity.LIGHT)!!, 0.0)
        assertEquals(5.0, Met.value(CardioType.OTHER, intensity = Intensity.MODERATE)!!, 0.0)
        assertEquals(8.0, Met.value(CardioType.OTHER, intensity = Intensity.VIGOROUS)!!, 0.0)
        // No picker defaults to moderate.
        assertEquals(5.0, Met.value(CardioType.OTHER)!!, 0.0)
    }

    @Test
    fun `walking and running have no MET`() {
        assertNull(Met.value(CardioType.RUNNING))
        assertNull(Met.value(CardioType.WALKING))
        assertNull(Met.kcal(CardioType.RUNNING, 68.0, 1_800))
    }

    @Test
    fun `zero weight or duration gives null`() {
        assertNull(Met.kcal(CardioType.CYCLING, 0.0, 1_800))
        assertNull(Met.kcal(CardioType.CYCLING, 68.0, 0))
    }
}
