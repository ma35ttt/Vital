package com.vital.ai

/**
 * What a photo of a treadmill (or similar cardio machine) display says. Every field is
 * optional: a partly visible console should still fill whatever it can read.
 */
data class TreadmillReading(
    val durationSec: Int? = null,
    val distanceKm: Double? = null,
    val speedKmh: Double? = null,
    val inclinePct: Double? = null,
    val confidence: Double = 0.0
)

/**
 * Drops physically impossible values so a misread digit cannot poison the form. The bounds
 * are deliberately generous: they reject OCR noise, not unusual sessions.
 */
fun TreadmillReading.sanitized(): TreadmillReading = copy(
    durationSec = durationSec?.takeIf { it in 1..21_600 },
    distanceKm = distanceKm?.takeIf { it > 0.0 && it <= 300.0 },
    speedKmh = speedKmh?.takeIf { it > 0.0 && it <= 45.0 },
    inclinePct = inclinePct?.takeIf { it in 0.0..40.0 },
    confidence = confidence.coerceIn(0.0, 1.0)
)

const val TREADMILL_PROMPT: String =
    """Read the numbers on this cardio-machine display (treadmill, or a similar console).
Return ONLY a JSON object with:
{
  "durationSec": total elapsed time in seconds,
  "distanceKm": distance in kilometres,
  "speedKmh": speed in kilometres per hour,
  "inclinePct": incline or gradient in percent,
  "confidence": 0.0-1.0
}
Convert units from the display when needed (mph -> km/h, miles -> km).
Use null for a value that is not visible. Do not guess.
Return ONLY the JSON, no markdown, no extra text."""