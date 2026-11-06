package com.vital.ui

import com.vital.ai.TreadmillReading
import org.junit.Assert.assertEquals
import org.junit.Test

class CardioPhotoTest {

    @Test
    fun `maps every visible console value onto a Run field`() {
        val values = treadmillDraftValues(
            TreadmillReading(
                durationSec = 1_930,
                distanceKm = 3.22,
                speedKmh = 6.0,
                inclinePct = 2.0,
                confidence = 0.9
            )
        )

        assertEquals("32:10", values[CardioField.DURATION])
        assertEquals("3.22", values[CardioField.DISTANCE_KM])
        assertEquals("6", values[CardioField.SPEED])
        assertEquals("2", values[CardioField.INCLINE])
    }

    @Test
    fun `leaves unread fields untouched`() {
        val values = treadmillDraftValues(TreadmillReading(speedKmh = 8.5, confidence = 0.8))

        assertEquals(mapOf(CardioField.SPEED to "8.5"), values)
    }

    @Test
    fun `trims redundant decimals without losing precision`() {
        assertEquals("3.2", trimDecimal(3.20, 2))
        assertEquals("3.22", trimDecimal(3.219, 2))
        assertEquals("12", trimDecimal(12.0, 1))
        assertEquals("0.5", trimDecimal(0.5, 1))
    }
}