package com.vital.ui

import com.vital.data.entity.CardioType
import com.vital.data.entity.Intensity
import com.vital.data.entity.StrokeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardioEntryTest {

    @Test
    fun `pace converts to speed and back`() {
        // 2:30 per 500 m -> 12 km/h.
        assertEquals(12.0, paceToSpeedKmh("2:30")!!, 0.001)
        assertEquals(150, speedToPace(12.0))
        assertNull(paceToSpeedKmh(null))
    }

    @Test
    fun `rowing derives distance from pace and duration`() {
        val entry = parseCardioEntry(
            CardioType.ROWING,
            mapOf(
                CardioField.DURATION to "30:00",
                CardioField.PACE to "2:30",
                CardioField.CADENCE to "22"
            )
        )!!
        assertEquals(1_800, entry.durationSec)
        assertEquals(12.0, entry.speedKmh!!, 0.01)
        // 12 km/h for 30 minutes is 6 km.
        assertEquals(6.0, entry.distanceKm!!, 0.01)
        assertEquals(22.0, entry.cadenceSpm!!, 0.0)
    }

    @Test
    fun `swim distance is entered in metres`() {
        val entry = parseCardioEntry(
            CardioType.SWIMMING,
            mapOf(
                CardioField.DURATION to "35:00",
                CardioField.DISTANCE_M to "1200",
                CardioField.LAPS to "48"
            )
        )!!
        assertEquals(1.2, entry.distanceKm!!, 0.001)
        assertEquals(48, entry.laps)
        assertEquals(StrokeType.FREESTYLE, entry.stroke)
        assertNull(entry.intensity)
    }

    @Test
    fun `swim stroke is carried through`() {
        val entry = parseCardioEntry(
            CardioType.SWIMMING,
            mapOf(
                CardioField.DURATION to "20:00",
                CardioField.STROKE to StrokeType.BUTTERFLY.name
            )
        )!!
        assertEquals(StrokeType.BUTTERFLY, entry.stroke)
    }

    @Test
    fun `other defaults to moderate`() {
        val entry = parseCardioEntry(
            CardioType.OTHER,
            mapOf(CardioField.DURATION to "40:00", CardioField.DISTANCE_KM to "3")
        )!!
        assertEquals(Intensity.MODERATE, entry.intensity)
        assertNull(entry.stroke)
    }

    @Test
    fun `a run infers distance from speed`() {
        val entry = parseCardioEntry(
            CardioType.RUNNING,
            mapOf(CardioField.DURATION to "30:00", CardioField.SPEED to "12")
        )!!
        assertEquals(12.0, entry.speedKmh!!, 0.001)
        assertEquals(6.0, entry.distanceKm!!, 0.001)
        assertNull(entry.level)
    }

    @Test
    fun `missing duration is not an entry`() {
        assertNull(parseCardioEntry(CardioType.CYCLING, mapOf(CardioField.SPEED to "24")))
    }

    @Test
    fun `session kcal dispatches by activity`() {
        val cycling = parseCardioEntry(
            CardioType.CYCLING,
            mapOf(CardioField.DURATION to "40:00")
        )!!
        assertEquals(333.2, estimateSessionKcal(CardioType.CYCLING, cycling, 68.0)!!, 0.5)

        val running = parseCardioEntry(
            CardioType.RUNNING,
            mapOf(
                CardioField.DURATION to "32:10",
                CardioField.SPEED to "6",
                CardioField.INCLINE to "2"
            )
        )!!
        assertEquals(187.0, estimateSessionKcal(CardioType.RUNNING, running, 68.0)!!, 0.5)
    }

    @Test
    fun `picker cycles through every option`() {
        var name: String? = null
        val seen = mutableSetOf<String>()
        repeat(StrokeType.entries.size) {
            name = cyclePicker(CardioField.STROKE, name)
            seen += name!!
        }
        assertEquals(StrokeType.entries.map { it.name }.toSet(), seen)
    }
}
